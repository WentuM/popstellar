package election

import (
	"encoding/base64"
	"encoding/json"
	"popstellar/channel"
	"popstellar/channel/registry"
	"popstellar/crypto"
	"popstellar/inbox"
	jsonrpc "popstellar/message"
	"popstellar/message/answer"
	"popstellar/message/messagedata"
	"popstellar/message/query"
	"popstellar/message/query/method"
	"popstellar/message/query/method/message"
	"popstellar/network/socket"
	"popstellar/validation"
	"strconv"
	"sync"

	"github.com/rs/zerolog"
	"go.dedis.ch/kyber/v3"
	"golang.org/x/xerrors"
)

const msgID = "msg id"
const failedToDecodeData = "failed to decode message data: %v"

// attendees represents the attendees in an election.
type attendees struct {
	sync.Mutex
	store map[string]struct{}
}

// isPresent checks if a key representing a user is present in
// the list of attendees.
func (a *attendees) isPresent(key string) bool {
	a.Lock()
	defer a.Unlock()

	_, ok := a.store[key]
	return ok
}

// NewChannel returns a new initialized election channel
func NewChannel(channelPath string, start, end int64, started bool, questions []messagedata.ElectionSetupQuestion,
	attendeesMap map[string]struct{}, hub channel.HubFunctionalities, log zerolog.Logger,
	organizerPubKey kyber.Point) channel.Channel {

	log = log.With().Str("channel", "election").Logger()

	// Saving on election channel too so it self-contains the entire election history
	// electionCh.inbox.storeMessage(msg)

	newChannel := &Channel{
		sockets:   channel.NewSockets(),
		inbox:     inbox.NewInbox(channelPath),
		channelID: channelPath,

		start:      start,
		end:        end,
		started:    started,
		terminated: false,
		questions:  getAllQuestionsForElectionChannel(questions),

		attendees: &attendees{
			store: attendeesMap,
		},

		hub: hub,

		log: log,

		organiserPubKey: organizerPubKey,
	}

	newChannel.registry = newChannel.NewElectionRegistry()

	return newChannel
}

// NewElectionRegistry creates a new registry for the election channel
func (c *Channel) NewElectionRegistry() registry.MessageRegistry {
	registry := registry.NewMessageRegistry()

	registry.Register(messagedata.ElectionOpen{}, c.processElectionOpen)
	registry.Register(messagedata.VoteCastVote{}, c.processCastVote)
	registry.Register(messagedata.ElectionEnd{}, c.processElectionEnd)
	registry.Register(messagedata.ElectionResult{}, c.processElectionResult)

	return registry
}

// Channel is used to handle election messages.
type Channel struct {
	sockets   channel.Sockets
	inbox     *inbox.Inbox
	channelID string

	// *baseChannel

	// Starting time of the election
	start int64

	// Ending time of the election
	end int64

	// True if the election has started and false otherwise
	started bool

	// True if the election is over and false otherwise
	terminated bool

	// Questions asked to the participants
	//the key will be the string representation of the id of type byte[]
	questions map[string]*question

	// attendees that took part in the roll call string of their PK
	attendees *attendees

	hub channel.HubFunctionalities

	log zerolog.Logger

	organiserPubKey kyber.Point

	registry registry.MessageRegistry
}

// question represents a question in an election.
type question struct {
	// ID represents the id of the question.
	id []byte

	// ballotOptions represents different ballot options.
	ballotOptions []string

	//valid vote mutex.
	validVotesMu sync.RWMutex

	// validVotes represents the list of all valid votes. The key represents
	// the public key of the person casting the vote.
	validVotes map[string]validVote

	// method represents the voting method of the election. Either "Plurality"
	// or "Approval".
	method string
}

type validVote struct {
	// msgID represents the ID of the message containing the cast vote
	msgID string

	// ID represents the ID of the valid cast vote
	ID string

	// voteTime represents the time of the creation of the vote
	voteTime int64

	// indexes represents the indexes of the ballot options
	indexes []int
}

// Publish is used to handle publish messages in the election channel.
func (c *Channel) Publish(publish method.Publish, _ socket.Socket) error {
	c.log.Info().
		Str(msgID, strconv.Itoa(publish.ID)).
		Msg("received a publish")

	err := c.verifyMessage(publish.Params.Message)
	if err != nil {
		return xerrors.Errorf("failed to verify publish message on an "+
			"election channel: %w", err)
	}

	err = c.handleMessage(publish.Params.Message)
	if err != nil {
		return xerrors.Errorf("failed to handle a publish message: %v", err)
	}

	return nil
}

// Subscribe is used to handle a subscribe message from the client.
func (c *Channel) Subscribe(socket socket.Socket, msg method.Subscribe) error {
	c.log.Info().
		Str(msgID, strconv.Itoa(msg.ID)).
		Msg("received a subscribe")
	c.sockets.Upsert(socket)

	return nil
}

// Unsubscribe is used to handle an unsubscribe message.
func (c *Channel) Unsubscribe(socketID string, msg method.Unsubscribe) error {
	c.log.Info().
		Str(msgID, strconv.Itoa(msg.ID)).
		Msg("received an unsubscribe")

	ok := c.sockets.Delete(socketID)

	if !ok {
		return answer.NewError(-2, "client is not subscribed to this channel")
	}

	return nil
}

// Catchup is used to handle a catchup message.
func (c *Channel) Catchup(catchup method.Catchup) []message.Message {
	c.log.Info().
		Str(msgID, strconv.Itoa(catchup.ID)).
		Msg("received a catchup")

	return c.inbox.GetSortedMessages()
}

// Broadcast is used to handle a broadcast message.
func (c *Channel) Broadcast(broadcast method.Broadcast, _ socket.Socket) error {
	c.log.Info().Msg("received a broadcast")

	err := c.verifyMessage(broadcast.Params.Message)
	if err != nil {
		return xerrors.Errorf("failed to verify broadcast message on an "+
			"election channel: %w", err)
	}

	err = c.handleMessage(broadcast.Params.Message)
	if err != nil {
		return xerrors.Errorf("failed to handle broadcast message: %v", err)
	}

	return nil
}

// handleMessage handles a message received in a broadcast or publish method
func (c *Channel) handleMessage(msg message.Message) error {

	err := c.registry.Process(msg)
	if err != nil {
		return xerrors.Errorf("failed to process message: %v", err)
	}

	c.inbox.StoreMessage(msg)

	return nil
}

// broadcastToAllClients is a helper message to broadcast a message to all
// subscribers.
func (c *Channel) broadcastToAllClients(msg message.Message) error {
	c.log.Info().
		Str(msgID, msg.MessageID).
		Msg("broadcasting message to all clients")

	rpcMessage := method.Broadcast{
		Base: query.Base{
			JSONRPCBase: jsonrpc.JSONRPCBase{
				JSONRPC: "2.0",
			},
			Method: query.MethodBroadcast,
		},
		Params: struct {
			Channel string          `json:"channel"`
			Message message.Message `json:"message"`
		}{
			c.channelID,
			msg,
		},
	}

	buf, err := json.Marshal(&rpcMessage)
	if err != nil {
		return xerrors.Errorf("failed to marshal broadcast query: %v", err)
	}

	c.sockets.SendToAll(buf)

	return nil
}

// verifyMessage checks if a message in a Publish or Broadcast method is valid
func (c *Channel) verifyMessage(msg message.Message) error {

	jsonData, err := base64.URLEncoding.DecodeString(msg.Data)
	if err != nil {
		return xerrors.Errorf(failedToDecodeData, err)
	}

	// Verify the data
	err = c.hub.GetSchemaValidator().VerifyJSON(jsonData, validation.Data)
	if err != nil {
		return xerrors.Errorf("failed to verify json schema: %w", err)
	}

	// Check if the message already exists
	_, ok := c.inbox.GetMessage(msg.MessageID)
	if ok {
		return answer.NewError(-3, "message already exists")
	}

	return nil
}

func (c *Channel) processElectionOpen(msg message.Message, msgData interface{}) error {

	_, ok := msgData.(*messagedata.ElectionOpen)
	if !ok {
		return xerrors.Errorf("message %v isn't a election#open message", msgData)
	}

	c.log.Info().Msg("received a election#open message")

	senderBuf, err := base64.URLEncoding.DecodeString(msg.Sender)
	if err != nil {
		return xerrors.Errorf("failed to decode sender key: %v", err)
	}

	senderPoint := crypto.Suite.Point()
	err = senderPoint.UnmarshalBinary(senderBuf)
	if err != nil {
		return answer.NewError(-4, "invalid sender public key")
	}

	if !c.organiserPubKey.Equal(senderPoint) {
		return answer.NewErrorf(-5, "sender is %s, should be the organizer", msg.Sender)
	}

	var electionOpen messagedata.ElectionOpen

	err = msg.UnmarshalData(&electionOpen)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal publish election end: %v", err)
	}

	// check that data is correct
	err = c.verifyMessageElectionOpen(electionOpen)
	if err != nil {
		return xerrors.Errorf("invalid election#open message: %v", err)
	}

	c.started = true

	err = c.broadcastToAllClients(msg)
	if err != nil {
		return xerrors.Errorf("failed to broadcast message: %v", err)
	}

	return nil
}

// processCastVote is the callback that processes election#cast_vote messages
func (c *Channel) processCastVote(msg message.Message, msgData interface{}) error {

	_, ok := msgData.(*messagedata.VoteCastVote)
	if !ok {
		return xerrors.Errorf("message '%T' isn't a election#cast_vote message", msgData)
	}

	c.log.Info().Msg("received a election#cast_vote message")

	senderBuf, err := base64.URLEncoding.DecodeString(msg.Sender)
	if err != nil {
		return xerrors.Errorf("failed to decode sender key: %v", err)
	}

	senderPoint := crypto.Suite.Point()
	err = senderPoint.UnmarshalBinary(senderBuf)
	if err != nil {
		return answer.NewError(-4, "invalid sender public key")
	}

	// verify sender is an attendee or the organizer
	ok = c.attendees.isPresent(msg.Sender) || c.organiserPubKey.Equal(senderPoint)
	if !ok {
		return answer.NewError(-4, "only attendees can cast a vote in an election")
	}

	var castVote messagedata.VoteCastVote

	err = msg.UnmarshalData(&castVote)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal cast vote: %v", err)
	}

	err = c.verifyMessageCastVote(castVote)
	if err != nil {
		return xerrors.Errorf("invalid election#cast_vote message: %v", err)
	}

	err = updateVote(msg.MessageID, msg.Sender, castVote, c.questions)
	if err != nil {
		return xerrors.Errorf("failed to update vote: %v", err)
	}

	err = c.broadcastToAllClients(msg)
	if err != nil {
		return xerrors.Errorf("failed to broadcast message: %v", err)
	}

	return nil
}

// processElectionEnd is the callback that processes election#end messages
func (c *Channel) processElectionEnd(msg message.Message, msgData interface{}) error {

	_, ok := msgData.(*messagedata.ElectionEnd)
	if !ok {
		return xerrors.Errorf("message '%T' isn't a election#end message", msgData)
	}

	c.log.Info().Msg("received a election#end message")

	sender := msg.Sender
	senderBuf, err := base64.URLEncoding.DecodeString(sender)
	if err != nil {
		c.log.Error().Msgf("problem decoding sender public key: %v", err)
		return xerrors.Errorf("sender is %s, should be base64", sender)
	}

	// check sender is a valid public key
	senderPoint := crypto.Suite.Point()
	err = senderPoint.UnmarshalBinary(senderBuf)
	if err != nil {
		c.log.Error().Msgf("public key unmarshal problem: %v", err)
		return answer.NewErrorf(-4, "sender is %s, should be a valid public key: %v", sender, err)
	}

	// check sender of the election end message is the organizer
	if !c.organiserPubKey.Equal(senderPoint) {
		return answer.NewErrorf(-5, "sender is %s, should be the organizer", msg.Sender)
	}

	var electionEnd messagedata.ElectionEnd

	err = msg.UnmarshalData(&electionEnd)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal publish election end: %v", err)
	}

	// check that data is correct
	err = c.verifyMessageElectionEnd(electionEnd)
	if err != nil {
		return xerrors.Errorf("invalid election#end message: %v", err)
	}

	c.started = false
	c.terminated = true

	err = c.broadcastToAllClients(msg)
	if err != nil {
		return xerrors.Errorf("failed to broadcast message: %v", err)
	}

	// broadcast election result message
	err = c.broadcastElectionResult()
	if err != nil {
		return xerrors.Errorf("problem sending election#result message: %v", err)
	}

	return nil
}

// processElectionResult is the callback that processes election#result messages
func (c *Channel) processElectionResult(msg message.Message, msgData interface{}) error {
	data, ok := msgData.(*messagedata.ElectionResult)
	if !ok {
		return xerrors.Errorf("message '%T' isn't a election#result message", msgData)
	}

	c.log.Info().Msg("received a election#result message")

	// verify that the question ids are base64URL encoded
	for i, q := range data.Questions {
		_, err := base64.URLEncoding.DecodeString(q.ID)
		if err != nil {
			return xerrors.Errorf("invalid election#result message: question "+
				"id %d %s, should be base64URL encoded", i, q.ID)
		}
	}

	return nil
}

// broadcastElectionResult gathers and broadcasts the results of an election
func (c *Channel) broadcastElectionResult() error {

	resultElection, err := gatherResults(c.questions, c.log)
	if err != nil {
		return xerrors.Errorf("failed to gather results: %v", err)
	}

	dataBuf, err := json.Marshal(&resultElection)
	if err != nil {
		return xerrors.Errorf("failed to marshal the data: %v", err)
	}

	newData64 := base64.URLEncoding.EncodeToString(dataBuf)

	pk := c.hub.GetPubKeyServ()
	pkBuf, err := pk.MarshalBinary()
	if err != nil {
		return xerrors.Errorf("failed to marshal the public key: %v", err)
	}

	// Sign the data
	signatureBuf, err := c.hub.Sign(dataBuf)
	if err != nil {
		return xerrors.Errorf("failed to sign the data: %v", err)
	}

	signature := base64.URLEncoding.EncodeToString(signatureBuf)

	electionResultMsg := message.Message{
		Data:              newData64,
		Sender:            base64.URLEncoding.EncodeToString(pkBuf),
		Signature:         signature,
		MessageID:         messagedata.Hash(newData64, signature),
		WitnessSignatures: []message.WitnessSignature{},
	}

	c.broadcastToAllClients(electionResultMsg)

	return nil
}

func gatherResults(questions map[string]*question, log zerolog.Logger) (messagedata.ElectionResult, error) {
	log.Info().Msgf("gathering results for the election")

	resultElection := messagedata.ElectionResult{
		Object:    messagedata.ElectionObject,
		Action:    messagedata.ElectionActionResult,
		Questions: []messagedata.ElectionResultQuestion{},
	}

	for id := range questions {
		question, ok := questions[id]
		if !ok {
			return resultElection, xerrors.Errorf("no question with this questionId '%s' was recorded", id)
		}

		votes := question.validVotes
		if question.method == "Plurality" {
			numberOfVotesPerBallotOption := make([]int, len(question.ballotOptions))
			for _, vote := range votes {
				for _, ballotIndex := range vote.indexes {
					numberOfVotesPerBallotOption[ballotIndex]++
				}
			}

			res := gatherOptionCounts(numberOfVotesPerBallotOption, question.ballotOptions)

			electResult := messagedata.ElectionResultQuestion{
				ID:     id,
				Result: res,
			}

			resultElection.Questions = append(resultElection.Questions, electResult)

			log.Info().
				Str("question id", id).
				Msg("appending a question with the count and result")
		}
	}

	return resultElection, nil
}

func gatherOptionCounts(count []int, options []string) []messagedata.ElectionResultQuestionResult {
	questionResults := make([]messagedata.ElectionResultQuestionResult, 0)
	for i, option := range options {
		questionResults = append(questionResults, messagedata.ElectionResultQuestionResult{
			BallotOption: option,
			Count:        count[i],
		})
	}

	return questionResults
}

func checkMethodProperties(method string, length int) error {
	if method == "Plurality" && length < 1 {
		return answer.NewError(-4, "No ballot option was chosen for plurality voting method")
	}
	if method == "Approval" && length != 1 {
		return answer.NewError(-4, "Cannot choose multiple ballot options on approval voting method")
	}

	return nil
}

func updateVote(msgID string, sender string, castVote messagedata.VoteCastVote, questions map[string]*question) error {
	// this should update any previously set vote if the message ids are the same
	for _, vote := range castVote.Votes {

		qs, ok := questions[vote.Question]
		if !ok {
			return answer.NewErrorf(-4, "no Question with question ID %s exists", vote.Question)
		}

		// this is to handle the case when the organizer must handle multiple
		// votes being cast at the same time
		qs.validVotesMu.Lock()
		earlierVote, ok := qs.validVotes[sender]

		// if the sender didn't previously cast a vote or if the vote is no
		// longer valid update it
		err := checkMethodProperties(qs.method, len(vote.Vote))
		if err != nil {
			return xerrors.Errorf("failed to validate voting method props: %w", err)
		}

		if !ok || earlierVote.voteTime > castVote.CreatedAt {
			qs.validVotes[sender] = validVote{
				msgID,
				vote.ID,
				castVote.CreatedAt,
				vote.Vote,
			}
		}

		// other votes can now change the list of valid votes
		qs.validVotesMu.Unlock()
	}

	return nil
}

func getAllQuestionsForElectionChannel(questions []messagedata.ElectionSetupQuestion) map[string]*question {

	qs := make(map[string]*question)
	for _, q := range questions {
		ballotOpts := make([]string, len(q.BallotOptions))
		for i, b := range q.BallotOptions {
			ballotOpts[i] = b
		}

		qs[q.ID] = &question{
			id:            []byte(q.ID),
			ballotOptions: ballotOpts,
			validVotesMu:  sync.RWMutex{},
			validVotes:    make(map[string]validVote),
			method:        q.VotingMethod,
		}
	}

	return qs
}

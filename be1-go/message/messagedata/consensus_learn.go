package messagedata

// ConsensusLearn defines a message data
type ConsensusLearn struct {
	Object    string `json:"object"`
	Action    string `json:"action"`
	MessageID string `json:"message_id"`

	// CreatedAt is a Unix timestamp
	CreatedAt int64 `json:"created_at"`

	Value ValueLearn `json:"value"`

	Acceptors []string `json:"acceptors"`
}

type ValueLearn struct {
	Decision bool `json:"decision"`
}

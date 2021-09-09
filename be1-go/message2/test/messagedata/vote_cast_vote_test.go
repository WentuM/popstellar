package messagedata

import (
	"encoding/json"
	"os"
	"path/filepath"
	"student20_pop/message2/messagedata"
	"testing"

	"github.com/stretchr/testify/require"
)

func Test_Vote_Cast_Vote(t *testing.T) {
	file := filepath.Join(relativeExamplePath, "vote_cast_vote.json")

	buf, err := os.ReadFile(file)
	require.NoError(t, err)

	object, action, err := messagedata.GetObjectAndAction(buf)
	require.NoError(t, err)

	require.Equal(t, "election", object)
	require.Equal(t, "cast_vote", action)

	var msg messagedata.VoteCastVote

	err = json.Unmarshal(buf, &msg)
	require.NoError(t, err)

	require.Equal(t, "election", msg.Object)
	require.Equal(t, "cast_vote", msg.Action)
	require.Equal(t, "XXX", msg.Lao)
	require.Equal(t, "XXX", msg.Election)
	require.Equal(t, 123, msg.CreatedAt)

	require.Len(t, msg.Votes, 1)
	require.Equal(t, "XXX", msg.Votes[0].ID)
	require.Equal(t, "XXX", msg.Votes[0].Question)

	require.Len(t, msg.Votes[0].Vote, 1)
	require.Equal(t, 0, msg.Votes[0].Vote[0])
}

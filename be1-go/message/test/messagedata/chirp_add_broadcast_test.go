package messagedata

import (
	"encoding/json"
	"github.com/stretchr/testify/require"
	"os"
	"path/filepath"
	"popstellar/message/messagedata"
	"testing"
)

func Test_Chirp_Add_Broadcast(t *testing.T) {
	file := filepath.Join(relativeExamplePath, "chirp_add_broadcast", "chirp_add_broadcast.json")

	buf, err := os.ReadFile(file)
	require.NoError(t, err)

	object, action, err := messagedata.GetObjectAndAction(buf)
	require.NoError(t, err)

	require.Equal(t, "chirp", object)
	require.Equal(t, "add_broadcast", action)

	var msg messagedata.ChirpBroadcast

	err = json.Unmarshal(buf, &msg)
	require.NoError(t, err)

	require.Equal(t, "chirp", msg.Object)
	require.Equal(t, "add_broadcast", msg.Action)
	require.Equal(t, "/root/<lao_id>/social/<sender>", msg.Channel)
	require.Equal(t, "ONYYu9Q2kGdAVpfbGwdmgBPf4QBznjt-JQO2gGCL3iI=", msg.ChirpId)
	require.Equal(t, int64(1634760180), msg.Timestamp)

	err = msg.Verify()
	require.NoError(t, err)
}

func Test_Chirp_Add_Broadcast_Negative_Timestamp(t *testing.T) {
	file := filepath.Join(relativeExamplePath, "chirp_add_broadcast", "wrong_chirp_add_broadcast_negative_time.json")

	buf, err := os.ReadFile(file)
	require.NoError(t, err)

	object, action, err := messagedata.GetObjectAndAction(buf)
	require.NoError(t, err)

	require.Equal(t, "chirp", object)
	require.Equal(t, "add_broadcast", action)

	var msg messagedata.ChirpBroadcast

	err = json.Unmarshal(buf, &msg)
	require.NoError(t, err)

	require.Equal(t, "chirp", msg.Object)
	require.Equal(t, "add_broadcast", msg.Action)
	require.Equal(t, "/root/<lao_id>/social/<sender>", msg.Channel)
	require.Equal(t, "ONYYu9Q2kGdAVpfbGwdmgBPf4QBznjt-JQO2gGCL3iI=", msg.ChirpId)
	require.Equal(t, int64(-1), msg.Timestamp)

	err = msg.Verify()
	require.Error(t, err)
}

func Test_Chirp_Add_Broadcast_Not_Base64_Message(t *testing.T) {
	file := filepath.Join(relativeExamplePath, "chirp_add_broadcast", "wrong_chirp_add_broadcast_not_base_64_chirp_id.json")

	buf, err := os.ReadFile(file)
	require.NoError(t, err)

	object, action, err := messagedata.GetObjectAndAction(buf)
	require.NoError(t, err)

	require.Equal(t, "chirp", object)
	require.Equal(t, "add_broadcast", action)

	var msg messagedata.ChirpBroadcast

	err = json.Unmarshal(buf, &msg)
	require.NoError(t, err)

	require.Equal(t, "chirp", msg.Object)
	require.Equal(t, "add_broadcast", msg.Action)
	require.Equal(t, "/root/<lao_id>/social/<sender>", msg.Channel)
	require.Equal(t, "@@@", msg.ChirpId)
	require.Equal(t, int64(1634760180), msg.Timestamp)

	err = msg.Verify()
	require.Error(t, err)
}
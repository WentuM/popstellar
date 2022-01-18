package com.github.dedis.popstellar.model.network.method.message.data.socialmedia;

import static com.github.dedis.popstellar.Base64DataUtils.generateMessageID;
import static com.github.dedis.popstellar.Base64DataUtils.generateMessageIDOtherThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import com.github.dedis.popstellar.model.network.JsonTestUtils;
import com.github.dedis.popstellar.model.network.method.message.data.Action;
import com.github.dedis.popstellar.model.network.method.message.data.Objects;
import com.github.dedis.popstellar.model.objects.security.MessageID;
import com.google.gson.JsonParseException;

import org.junit.Test;

public class NotifyAddChirpTest {

  private static final MessageID CHIRP_ID = generateMessageID();
  private static final String CHANNEL = "/root/laoId/social/myChannel";
  private static final long TIMESTAMP = 1631280815;

  private static final NotifyAddChirp NOTIFY_ADD_CHIRP =
      new NotifyAddChirp(CHIRP_ID, CHANNEL, TIMESTAMP);

  @Test
  public void getObjectTest() {
    assertEquals(Objects.CHIRP.getObject(), NOTIFY_ADD_CHIRP.getObject());
  }

  @Test
  public void getActionTest() {
    assertEquals(Action.NOTIFY_ADD.getAction(), NOTIFY_ADD_CHIRP.getAction());
  }

  @Test
  public void getChirpIdTest() {
    assertEquals(CHIRP_ID, NOTIFY_ADD_CHIRP.getChirpId());
  }

  @Test
  public void getChannelTest() {
    assertEquals(CHANNEL, NOTIFY_ADD_CHIRP.getChannel());
  }

  @Test
  public void getTimestampTest() {
    assertEquals(TIMESTAMP, NOTIFY_ADD_CHIRP.getTimestamp());
  }

  @Test
  public void equalsTest() {
    assertEquals(NOTIFY_ADD_CHIRP, new NotifyAddChirp(CHIRP_ID, CHANNEL, TIMESTAMP));

    String random = "random";
    assertNotEquals(
        NOTIFY_ADD_CHIRP,
        new NotifyAddChirp(generateMessageIDOtherThan(CHIRP_ID), CHANNEL, TIMESTAMP));
    assertNotEquals(NOTIFY_ADD_CHIRP, new NotifyAddChirp(CHIRP_ID, random, TIMESTAMP));
    assertNotEquals(NOTIFY_ADD_CHIRP, new NotifyAddChirp(CHIRP_ID, CHANNEL, TIMESTAMP + 1));
  }

  @Test
  public void jsonValidationTest() {
    JsonTestUtils.testData(NOTIFY_ADD_CHIRP);

    String path =
        "protocol/examples/messageData/chirp_notify_add/wrong_chirp_notify_add_negative_time.json";
    assertThrows(JsonParseException.class, () -> JsonTestUtils.parse(path));
  }
}

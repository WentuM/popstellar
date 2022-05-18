package com.github.dedis.popstellar.model.network.method.message.data.election;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.github.dedis.popstellar.model.network.JsonTestUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CastVoteTest {

  private final String questionId1 = " myQuestion1";
  private final String questionId2 = " myQuestion2";
  private final String laoId = "myLao";
  private final String electionId = " myElection";
  private final boolean writeInEnabled = false;
  private final String write_in = "My write in ballot option";

  // Set up a open ballot election
  private final ElectionVote electionVote1 =
      new ElectionVote(questionId1, Arrays.asList(2, 1, 0), writeInEnabled, write_in, electionId);
  private final ElectionVote electionVote2 =
      new ElectionVote(questionId2, Arrays.asList(0, 1, 2), writeInEnabled, write_in, electionId);
  private final List<ElectionVote> electionVotes = Arrays.asList(electionVote1, electionVote2);

  // Set up a secret ballot election
  private final ElectionEncryptedVote electionEncryptedVote1 =
          new ElectionEncryptedVote(electionId, Arrays.asList("2","1","0"), write_in, writeInEnabled, questionId1);
  private final ElectionEncryptedVote electionEncryptedVote2 =
          new ElectionEncryptedVote(electionId, Arrays.asList("0","1","2"), write_in, writeInEnabled, questionId2);
  private final List<ElectionEncryptedVote> electionEncryptedVotes = Arrays.asList(electionEncryptedVote1, electionEncryptedVote2);

  private final CastVote castOpenVote = new CastVote(electionVotes, null, electionId, laoId);
  private final CastVote castEncryptedVote = new CastVote(null, electionEncryptedVotes, electionId, laoId);

  @Test
  public void getLaoIdTest() {
    assertThat(castOpenVote.getLaoId(), is(laoId));
    assertThat(castEncryptedVote.getLaoId(), is(laoId));
  }

  @Test
  public void getElectionIdTest() {
    assertThat(castOpenVote.getElectionId(), is(electionId));
    assertThat(castEncryptedVote.getElectionId(), is(laoId));
  }

  @Test
  public void getOpenBallotVotesTest() {
    assertThat(castOpenVote.getOpenBallotVotes(), is(electionVotes));
    assertThat(null, is(castEncryptedVote.getOpenBallotVotes()));
  }

  @Test
  public void getEncryptedBallotVotesTest() {
    assertThat(castEncryptedVote.getEncryptedVotes(), is(electionEncryptedVotes));
    assertThat(null, is(castOpenVote.getEncryptedVotes()));
  }

  @Test
  public void isEqualTest() {
    // Test an OPEN_BALLOT cast vote
    assertEquals(castOpenVote, new CastVote(electionVotes,null, electionId, laoId));
    assertEquals(castOpenVote, castOpenVote);
    assertNotEquals(
        castOpenVote, new CastVote(Collections.singletonList(electionVote1), null, electionId, laoId));
    assertNotEquals(
            castOpenVote, new CastVote(Collections.singletonList(electionVote1),null, "random", laoId));
    assertNotEquals(
             castOpenVote, new CastVote(Collections.singletonList(electionVote1),null, electionId, "random"));

    // Test a SECRET_BALLOT cast vote

  }

  @Test
  public void jsonValidationTest() {
    JsonTestUtils.testData(castOpenVote);
  }
}

package com.github.dedis.student20_pop.model.network.method.message.data.election;

import com.github.dedis.student20_pop.model.network.method.message.data.Action;
import com.github.dedis.student20_pop.model.network.method.message.data.Objects;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class ElectionSetupTest {

    private String electionSetupName = "new election setup";
    private long start = 0;
    private long end = 1;
    private String votingMethod = "Plurality";
    private boolean writeIn = false;
    private List<String> ballotOptions = Arrays.asList("candidate1", "candidate2");
    private String question = "which is the best ?";
    private String laoId = "my lao id";

    private ElectionSetup electionSetup = new ElectionSetup(electionSetupName, start, end, votingMethod, writeIn, ballotOptions, question, laoId);

    @Test
    public void electionSetupGetterReturnsCorrectName() {
        assertThat(electionSetup.getName(), is(electionSetupName));
    }

    @Test
    public void electionSetupGetterReturnsCorrectStartTime() {
        assertThat(electionSetup.getStartTime(), is(start));
    }

    @Test
    public void electionSetupGetterReturnsCorrectEndTime() {
        assertThat(electionSetup.getEndTime(), is(end));
    }

    @Test
    public void electionSetupGetterReturnsCorrectLaoId() {
        assertThat(electionSetup.getLao(), is(laoId));
    }

    @Test
    public void electionSetupOnlyOneQuestion() {
        assertThat(electionSetup.getQuestions().size(), is(1));
    }

    @Test
    public void electionSetupGetterReturnsCorrectObject() {
        assertThat(electionSetup.getObject(), is(Objects.ELECTION.getObject()));
    }

    @Test
    public void electionSetupGetterReturnsCorrectAction() {
        assertThat(electionSetup.getAction(), is(Action.SETUP.getAction()));
    }

    @Test
    public void fieldsCantBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(null, start, end, votingMethod, writeIn, ballotOptions, question, laoId);});
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, start, end, null, writeIn, ballotOptions, question, laoId);});
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, start, end, votingMethod, writeIn, null, question, laoId);});
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, start, end, votingMethod, writeIn, ballotOptions, null, laoId);});
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, start, end, votingMethod, writeIn, ballotOptions, question, null);});
    }

    @Test
    public void endCantHappenBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, 2, 1, votingMethod, writeIn, ballotOptions, question, laoId);});
    }

    @Test
    public void timestampsCantBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, -1, end, votingMethod, writeIn, ballotOptions, question, laoId);});
        assertThrows(IllegalArgumentException.class, () -> {ElectionSetup electionSetup = new ElectionSetup(electionSetupName, start, -1, votingMethod, writeIn, ballotOptions, question, laoId);});
    }

    @Test
    public void electionSetupGetterReturnsCorrectVersion() {
        assertThat(electionSetup.getVersion(), is("1.0.0"));
    }

    @Test
    public void electionSetupEqualsTrueForSameInstance() {
        assertThat(electionSetup.equals(electionSetup), is(true));
    }


}

package com.github.dedis.popstellar.ui.detail.event.election;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.dedis.popstellar.model.objects.event.EventState.CLOSED;
import static com.github.dedis.popstellar.model.objects.event.EventState.CREATED;
import static com.github.dedis.popstellar.model.objects.event.EventState.OPENED;
import static com.github.dedis.popstellar.model.objects.event.EventState.RESULTS_READY;
import static com.github.dedis.popstellar.testutils.Base64DataUtils.generateKeyPair;
import static com.github.dedis.popstellar.testutils.UITestUtils.dialogNegativeButton;
import static com.github.dedis.popstellar.testutils.UITestUtils.dialogPositiveButton;
import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.containerId;
import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.fragmentToOpenExtra;
import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.laoDetailValue;
import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.laoIdExtra;
import static com.github.dedis.popstellar.ui.pages.detail.event.election.ElectionFragmentPageObject.electionActionButton;
import static com.github.dedis.popstellar.ui.pages.detail.event.election.ElectionFragmentPageObject.electionFragmentEndTime;
import static com.github.dedis.popstellar.ui.pages.detail.event.election.ElectionFragmentPageObject.electionFragmentStartTime;
import static com.github.dedis.popstellar.ui.pages.detail.event.election.ElectionFragmentPageObject.electionFragmentStatus;
import static com.github.dedis.popstellar.ui.pages.detail.event.election.ElectionFragmentPageObject.electionFragmentTitle;
import static com.github.dedis.popstellar.ui.pages.detail.event.election.ElectionFragmentPageObject.electionManagementButton;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.dedis.popstellar.model.network.method.message.data.election.ElectionQuestion;
import com.github.dedis.popstellar.model.network.method.message.data.election.ElectionVersion;
import com.github.dedis.popstellar.model.objects.Election;
import com.github.dedis.popstellar.model.objects.Lao;
import com.github.dedis.popstellar.model.objects.security.KeyPair;
import com.github.dedis.popstellar.model.objects.security.PublicKey;
import com.github.dedis.popstellar.repository.LAORepository;
import com.github.dedis.popstellar.testutils.BundleBuilder;
import com.github.dedis.popstellar.testutils.fragment.ActivityFragmentScenarioRule;
import com.github.dedis.popstellar.ui.detail.LaoDetailActivity;
import com.github.dedis.popstellar.ui.detail.event.election.fragments.ElectionFragment;
import com.github.dedis.popstellar.utility.security.KeyManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoTestRule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import io.reactivex.subjects.BehaviorSubject;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class ElectionFragmentTest {

  private static final String LAO_NAME = "lao";
  private static final KeyPair SENDER_KEY = generateKeyPair();
  private static final PublicKey SENDER = SENDER_KEY.getPublicKey();
  private static final Lao LAO = new Lao(LAO_NAME, SENDER, 10223421);
  private static final String LAO_ID = LAO.getId();
  private static final String TITLE = "Election name";
  private static final long CREATION = 10323411;
  private static final long START = 10323421;
  private static final long END = 10323431;
  private static final Election election =
      new Election(LAO_ID, CREATION, TITLE, ElectionVersion.OPEN_BALLOT);

  ElectionQuestion electionQuestion1 =
      new ElectionQuestion(
          "ElectionQuestion", "Plurality", false, Arrays.asList("1", "2"), election.getId());
  ElectionQuestion electionQuestion2 =
      new ElectionQuestion(
          "ElectionQuestion2", "Plurality", false, Arrays.asList("a", "b"), election.getId());

  private static final DateFormat DATE_FORMAT =
      new SimpleDateFormat("dd/MM/yyyy HH:mm z", Locale.ENGLISH);

  @BindValue @Mock LAORepository repository;
  @BindValue @Mock KeyManager keyManager;

  @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

  @Rule(order = 0)
  public final MockitoTestRule mockitoRule = MockitoJUnit.testRule(this);

  @Rule(order = 1)
  public final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

  @Rule(order = 2)
  public final ExternalResource setupRule =
      new ExternalResource() {
        @Override
        protected void before() {
          hiltRule.inject();
          when(repository.getLaoObservable(anyString()))
              .thenReturn(BehaviorSubject.createDefault(LAO));

          when(keyManager.getMainPublicKey()).thenReturn(SENDER);

          election.setElectionQuestions(Arrays.asList(electionQuestion1, electionQuestion2));

          election.setStart(START);
          election.setEnd(END);
          election.setEventState(CREATED);
        }
      };

  @Rule(order = 3)
  public ActivityFragmentScenarioRule<LaoDetailActivity, ElectionFragment> activityScenarioRule =
      ActivityFragmentScenarioRule.launchIn(
          LaoDetailActivity.class,
          new BundleBuilder()
              .putString(laoIdExtra(), LAO_ID)
              .putString(fragmentToOpenExtra(), laoDetailValue())
              .build(),
          containerId(),
          ElectionFragment.class,
          () -> ElectionFragment.newInstance(election));

  @Test
  public void electionTitleMatches() {
    electionFragmentTitle().check(matches(withText(TITLE)));
  }

  @Test
  public void statusCreatedTests() {
    electionFragmentStatus().check(matches(withText("Closed")));
  }

  @Test
  public void datesDisplayedMatches() {
    Date startTime = new Date(election.getStartTimestampInMillis());
    Date endTime = new Date(election.getEndTimestampInMillis());
    String startTimeText = DATE_FORMAT.format(startTime);
    String endTimeText = DATE_FORMAT.format(endTime);

    electionFragmentStartTime().check(matches(withText(startTimeText)));
    electionFragmentEndTime().check(matches(withText(endTimeText)));
  }

  @Test
  public void managementButtonIsDisplayed() {
    electionManagementButton().check(matches(isDisplayed()));
  }

  @Test
  public void managementButtonCreatedTest() {
    electionManagementButton().check(matches(withText("OPEN")));
  }

  @Test
  public void actionButtonCreatedTest() {
    electionActionButton().check(matches(withText("VOTE")));
    electionActionButton().check(matches(not(isEnabled())));
  }

  @Test
  public void statusOpenTest() {
    openElection();
    electionFragmentStatus().check(matches(withText("Open")));
  }

  @Test
  public void managementButtonOpenTest() {
    openElection();
    electionManagementButton().check(matches(withText("CLOSE")));
  }

  @Test
  public void actionButtonOpenTest() {
    openElection();
    electionActionButton().check(matches(withText("VOTE")));
    electionActionButton().check(matches(isEnabled()));
  }

  @Test
  public void statusClosedTest() {
    closeElection();
    electionFragmentStatus().check(matches(withText("Waiting for results")));
  }

  @Test
  public void managementButtonClosedTest() {
    closeElection();
    electionManagementButton().check(matches(not(isDisplayed())));
  }

  @Test
  public void actionButtonClosedTest() {
    closeElection();
    electionActionButton().check(matches(withText("Results")));
    electionActionButton().check(matches(not(isEnabled())));
  }

  @Test
  public void statusResultsTest() {
    receiveResults();
    electionFragmentStatus().check(matches(withText("Finished")));
  }

  @Test
  public void managementButtonResultsTest() {
    receiveResults();
    electionManagementButton().check(matches(not(isDisplayed())));
  }

  @Test
  public void actionButtonResultsTest() {
    receiveResults();
    electionActionButton().check(matches(withText("Results")));
    electionActionButton().check(matches(isEnabled()));
  }

  @Test
  public void openButtonDisplaysDialogOnclick() {
    electionManagementButton().perform(click());
    assertThat(dialogPositiveButton(), allOf(withText("Yes"), isDisplayed()));
    assertThat(dialogNegativeButton(), allOf(withText("No"), isDisplayed()));
  }

  @Test
  public void closeButtonDisplaysDialogOnclick() {
    openElection();
    electionManagementButton().perform(click());
    assertThat(dialogPositiveButton(), allOf(withText("Yes"), isDisplayed()));
    assertThat(dialogNegativeButton(), allOf(withText("No"), isDisplayed()));
  }

  private void openElection() {
    election.setEventState(OPENED);
  }

  private void closeElection() {
    election.setEventState(CLOSED);
  }

  private void receiveResults() {
    election.setEventState(RESULTS_READY);
  }
}
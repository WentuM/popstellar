package com.github.dedis.popstellar.ui.detail;

import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.fragmentToOpenExtra;
import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.laoDetailValue;
import static com.github.dedis.popstellar.ui.pages.detail.LaoDetailActivityPageObject.laoIdExtra;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.dedis.popstellar.model.objects.Lao;
import com.github.dedis.popstellar.model.objects.RollCall;
import com.github.dedis.popstellar.model.objects.event.EventState;
import com.github.dedis.popstellar.repository.LAORepository;
import com.github.dedis.popstellar.testutils.Base64DataUtils;
import com.github.dedis.popstellar.testutils.BundleBuilder;
import com.github.dedis.popstellar.testutils.IntentUtils;
import com.github.dedis.popstellar.ui.detail.event.EventListAdapter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoTestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import io.reactivex.subjects.BehaviorSubject;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class EventListAdapterTest {
  private static final Lao LAO = new Lao("LAO", Base64DataUtils.generatePublicKey(), 10223421);
  private static final String LAO_ID = LAO.getId();
  private static final RollCall ROLL_CALL = new RollCall("12345");
  private static final RollCall ROLL_CALL2 = new RollCall("54321");

  @BindValue @Mock LAORepository repository;

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
          when(repository.getAllLaos())
              .thenReturn(BehaviorSubject.createDefault(Collections.singletonList(LAO)));
        }
      };

  @Rule(order = 3)
  public ActivityScenarioRule<LaoDetailActivity> activityScenarioRule =
      new ActivityScenarioRule<>(
          IntentUtils.createIntent(
              LaoDetailActivity.class,
              new BundleBuilder()
                  .putString(laoIdExtra(), LAO_ID)
                  .putString(fragmentToOpenExtra(), laoDetailValue())
                  .build()));

  @Test
  public void emptyAdapterTest() {
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              LaoDetailViewModel laoDetailViewModel = LaoDetailActivity.obtainViewModel(activity);
              EventListAdapter adapter =
                  new EventListAdapter(new ArrayList<>(), laoDetailViewModel, activity);
              assertEquals(3, adapter.getItemCount());
            });
  }

  @Test
  public void viewTypeAdapterTest() {
    ROLL_CALL.setState(EventState.OPENED);
    ROLL_CALL2.setState(EventState.CREATED);
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              LaoDetailViewModel laoDetailViewModel = LaoDetailActivity.obtainViewModel(activity);
              EventListAdapter adapter =
                  new EventListAdapter(
                      Arrays.asList(ROLL_CALL, ROLL_CALL2), laoDetailViewModel, activity);
              assertEquals(4, adapter.getItemCount());
              assertEquals(EventListAdapter.TYPE_HEADER, adapter.getItemViewType(0));
              assertEquals(EventListAdapter.TYPE_EVENT, adapter.getItemViewType(1));
              assertEquals(EventListAdapter.TYPE_HEADER, adapter.getItemViewType(2));
              assertEquals(EventListAdapter.TYPE_HEADER, adapter.getItemViewType(3));
            });
  }

  @Test
  public void replaceListTest() {
    ROLL_CALL.setState(EventState.OPENED);
    ROLL_CALL2.setState(EventState.CREATED);
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              LaoDetailViewModel laoDetailViewModel = LaoDetailActivity.obtainViewModel(activity);
              EventListAdapter adapter =
                  new EventListAdapter(
                      Arrays.asList(ROLL_CALL, ROLL_CALL2), laoDetailViewModel, activity);
              adapter.replaceList(Collections.singletonList(ROLL_CALL2));
              assertEquals(3, adapter.getItemCount());
              assertEquals(EventListAdapter.TYPE_HEADER, adapter.getItemViewType(0));
              assertEquals(EventListAdapter.TYPE_HEADER, adapter.getItemViewType(1));
              assertEquals(EventListAdapter.TYPE_HEADER, adapter.getItemViewType(2));
            });
  }
}
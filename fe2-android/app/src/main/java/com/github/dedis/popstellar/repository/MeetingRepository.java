package com.github.dedis.popstellar.repository;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.github.dedis.popstellar.model.objects.Meeting;
import com.github.dedis.popstellar.repository.database.AppDatabase;
import com.github.dedis.popstellar.repository.database.event.meeting.MeetingDao;
import com.github.dedis.popstellar.repository.database.event.meeting.MeetingEntity;
import com.github.dedis.popstellar.utility.ActivityUtils;
import com.github.dedis.popstellar.utility.error.UnknownMeetingException;

import java.util.*;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * This class is the repository of the meetings events
 *
 * <p>Its main purpose is to store meetings by lao
 */
@Singleton
public class MeetingRepository {
  public static final String TAG = MeetingRepository.class.getSimpleName();
  private final Map<String, LaoMeetings> meetingsByLao = new HashMap<>();

  private final MeetingDao meetingDao;

  private final CompositeDisposable disposables = new CompositeDisposable();

  @Inject
  public MeetingRepository(AppDatabase appDatabase, Application application) {
    meetingDao = appDatabase.meetingDao();
    Map<Lifecycle.Event, Consumer<Activity>> consumerMap = new EnumMap<>(Lifecycle.Event.class);
    consumerMap.put(Lifecycle.Event.ON_STOP, activity -> disposables.clear());
    application.registerActivityLifecycleCallbacks(
        ActivityUtils.buildLifecycleCallback(consumerMap));
  }

  /**
   * This add/replace the meeting in the repository
   *
   * @param meeting the meeting to add/replace
   */
  public void updateMeeting(String laoId, Meeting meeting) {
    if (laoId == null) {
      throw new IllegalArgumentException("Lao id is null");
    }
    if (meeting == null) {
      throw new IllegalArgumentException("Meeting is null");
    }
    Timber.tag(TAG).d("Adding meeting on lao %s : %s", laoId, meeting);

    MeetingEntity meetingEntity = new MeetingEntity(laoId, meeting);
    // Persist the meeting
    disposables.add(
        meetingDao
            .insert(meetingEntity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> Timber.tag(TAG).d("Successfully persisted meeting %s", meeting.getId()),
                err -> Timber.tag(TAG).e(err, "Error in persisting meeting %s", meeting.getId())));

    // Retrieve Lao data and add the meeting to it
    getLaoMeetings(laoId).update(meeting);
  }

  /**
   * This provides an observable of a meeting that triggers an update when modified
   *
   * @param laoId the id of the Lao
   * @param meetingId the id of the meeting
   * @return the observable wrapping the wanted meeting
   * @throws UnknownMeetingException if no meeting with the provided id could be found
   */
  public Observable<Meeting> getMeetingObservable(String laoId, String meetingId)
      throws UnknownMeetingException {
    return getLaoMeetings(laoId).getMeetingObservable(meetingId);
  }

  public Meeting getMeetingWithId(String laoId, String meetingId) throws UnknownMeetingException {
    return getLaoMeetings(laoId).getMeetingWithId(meetingId);
  }

  @NonNull
  private synchronized LaoMeetings getLaoMeetings(String laoId) {
    return meetingsByLao.computeIfAbsent(laoId, lao -> new LaoMeetings(this, laoId));
  }

  /**
   * @param laoId the id of the Lao whose meetings we want to observe
   * @return observable set of ids, corresponding to the set of meetings published on the given lao
   */
  public Observable<Set<Meeting>> getMeetingsObservableInLao(String laoId) {
    return getLaoMeetings(laoId).getMeetingsSubject();
  }

  private static final class LaoMeetings {
    private final MeetingRepository repository;
    private final String laoId;
    private boolean alreadyRetrieved = false;

    private final Map<String, Meeting> meetingById = new HashMap<>();

    // This allows to observe a specific meeting(s)
    private final Map<String, Subject<Meeting>> meetingSubjects = new HashMap<>();

    // This allows to observe the collection of meetings as a whole
    private final Subject<Set<Meeting>> meetingsSubject =
        BehaviorSubject.createDefault(unmodifiableSet(emptySet()));

    public LaoMeetings(MeetingRepository repository, String laoId) {
      this.repository = repository;
      this.laoId = laoId;
    }

    /**
     * This either updates the meeting in the repository or adds it if absent
     *
     * @param meeting the meeting to update/add
     */
    public synchronized void update(Meeting meeting) {
      // Updating repo data
      String id = meeting.getId();
      meetingById.put(id, meeting);

      // Publish new values on subjects
      if (meetingSubjects.containsKey(id)) {
        // If it exist we update the subject
        Timber.tag(TAG).d("Updating existing meeting %s", meeting.getName());
        meetingSubjects.get(id).onNext(meeting);
      } else {
        // If it does not, we create a new subject
        Timber.tag(TAG).d("New meeting, subject created for %s", meeting.getName());
        meetingSubjects.put(id, BehaviorSubject.createDefault(meeting));
      }

      meetingsSubject.onNext(unmodifiableSet(new HashSet<>(meetingById.values())));
    }

    public Meeting getMeetingWithId(String id) throws UnknownMeetingException {
      if (!meetingById.containsKey(id)) {
        throw new UnknownMeetingException(id);
      }
      return meetingById.get(id);
    }

    public Observable<Meeting> getMeetingObservable(String id) throws UnknownMeetingException {
      Observable<Meeting> observable = meetingSubjects.get(id);
      if (observable == null) {
        throw new UnknownMeetingException(id);
      } else {
        return observable;
      }
    }

    public Observable<Set<Meeting>> getMeetingsSubject() {
      loadStorage();
      return meetingsSubject;
    }

    /**
     * Load in memory the meetings from the disk only when the user clicks on the respective LAO.
     * This can be done one time only, as from then things are stored in memory.
     */
    private void loadStorage() {
      if (alreadyRetrieved) {
        return;
      }
      repository.disposables.add(
          repository
              .meetingDao
              .getMeetingsByLaoId(laoId, meetingById.keySet())
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                  meetings ->
                      meetings.forEach(
                          meeting -> {
                            update(meeting);
                            Timber.tag(TAG).d("Retrieved from db meeting %s", meeting.getId());
                          }),
                  err ->
                      Timber.tag(TAG).e(err, "No meeting found in the storage for lao %s", laoId)));
      alreadyRetrieved = true;
    }
  }
}

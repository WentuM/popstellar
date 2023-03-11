package com.github.dedis.popstellar.ui.lao.event;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.github.dedis.popstellar.model.objects.event.Event;
import com.github.dedis.popstellar.repository.ElectionRepository;
import com.github.dedis.popstellar.repository.RollCallRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class EventsViewModel extends AndroidViewModel {

  private final ElectionRepository electionRepo;
  private final RollCallRepository rollCallRepo;

  private Observable<Set<Event>> events;

  @Inject
  public EventsViewModel(
      @NonNull Application application,
      RollCallRepository rollCallRepo,
      ElectionRepository electionRepo) {
    super(application);
    this.rollCallRepo = rollCallRepo;
    this.electionRepo = electionRepo;
  }

  public void setId(String laoId) {
    this.events =
        Observable.combineLatest(
                rollCallRepo.getRollCallsObservableInLao(laoId),
                electionRepo.getElectionsObservableInLao(laoId),
                (rcs, elecs) -> {
                  Set<Event> union = new HashSet<>(rcs);
                  union.addAll(elecs);
                  return union;
                })
            // Only dispatch the latest element once every 50 milliseconds
            // This avoids multiple updates in a short period of time
            .throttleLatest(50, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<Set<Event>> getEvents() {
    return events;
  }
}
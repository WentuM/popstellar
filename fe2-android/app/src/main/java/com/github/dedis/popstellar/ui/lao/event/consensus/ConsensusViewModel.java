package com.github.dedis.popstellar.ui.lao.event.consensus;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.github.dedis.popstellar.model.network.method.message.MessageGeneral;
import com.github.dedis.popstellar.model.network.method.message.data.consensus.ConsensusElect;
import com.github.dedis.popstellar.model.network.method.message.data.consensus.ConsensusElectAccept;
import com.github.dedis.popstellar.model.objects.*;
import com.github.dedis.popstellar.model.objects.security.MessageID;
import com.github.dedis.popstellar.model.objects.security.PublicKey;
import com.github.dedis.popstellar.repository.ConsensusRepository;
import com.github.dedis.popstellar.repository.remote.GlobalNetworkManager;
import com.github.dedis.popstellar.utility.security.KeyManager;
import com.google.gson.Gson;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.*;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

@HiltViewModel
public class ConsensusViewModel extends AndroidViewModel {
  private static final String TAG = ConsensusViewModel.class.getSimpleName();

  private String laoId;

  private final ConsensusRepository consensusRepo;
  private final GlobalNetworkManager networkManager;
  private final KeyManager keyManager;
  private final Gson gson;

  @Inject
  public ConsensusViewModel(
      @NonNull Application application,
      ConsensusRepository consensusRepo,
      GlobalNetworkManager networkManager,
      KeyManager keyManager,
      Gson gson) {
    super(application);
    this.consensusRepo = consensusRepo;
    this.networkManager = networkManager;
    this.keyManager = keyManager;
    this.gson = gson;
  }

  /**
   * Sends a ConsensusElect message.
   *
   * <p>Publish a GeneralMessage containing ConsensusElect data.
   *
   * @param creation the creation time of the consensus
   * @param objId the id of the object the consensus refers to (e.g. election_id)
   * @param type the type of object the consensus refers to (e.g. election)
   * @param property the property the value refers to (e.g. "state")
   * @param value the proposed new value for the property (e.g. "started")
   * @return A single emitting the published message
   */
  public Single<MessageGeneral> sendConsensusElect(
      long creation, String objId, String type, String property, Object value) {
    Timber.tag(TAG)
        .d("creating a new consensus for type: %s, property: %s, value: %s", type, property, value);

    Channel channel = Channel.getLaoChannel(laoId).subChannel("consensus");
    ConsensusElect consensusElect = new ConsensusElect(creation, objId, type, property, value);

    MessageGeneral msg = new MessageGeneral(keyManager.getMainKeyPair(), consensusElect, gson);

    return networkManager.getMessageSender().publish(channel, msg).toSingleDefault(msg);
  }

  /**
   * Sends a ConsensusElectAccept message.
   *
   * <p>Publish a GeneralMessage containing ConsensusElectAccept data.
   *
   * @param electInstance the corresponding ElectInstance
   * @param accept true if accepted, false if rejected
   */
  public Completable sendConsensusElectAccept(ElectInstance electInstance, boolean accept) {
    MessageID messageId = electInstance.getMessageId();
    Timber.tag(TAG)
        .d(
            "sending a new elect_accept for consensus with messageId : %s with value %s",
            messageId, accept);

    ConsensusElectAccept consensusElectAccept =
        new ConsensusElectAccept(electInstance.getInstanceId(), messageId, accept);

    return networkManager
        .getMessageSender()
        .publish(keyManager.getMainKeyPair(), electInstance.getChannel(), consensusElectAccept);
  }

  public void setLaoId(String laoId) {
    this.laoId = laoId;
  }

  public Observable<List<ConsensusNode>> getNodesByChannel(Channel laoChannel) {
    return consensusRepo.getNodesByChannel(laoChannel);
  }

  public ConsensusNode getNodeByLao(String laoId, PublicKey publicKey) {
    return consensusRepo.getNodeByLao(laoId, publicKey);
  }
}

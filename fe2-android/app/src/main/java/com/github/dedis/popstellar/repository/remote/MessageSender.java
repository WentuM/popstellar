package com.github.dedis.popstellar.repository.remote;

import com.github.dedis.popstellar.model.network.method.message.data.Data;
import com.github.dedis.popstellar.model.objects.security.KeyPair;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

/**
 * Interface exposing the first layer of the protocol.
 *
 * <p>It can be used to send requests to the linked backend and wait for answers. {@link
 * Completable} are used as callbacks to give the result of the request.
 */
public interface MessageSender extends Disposable {

  /**
   * Catchup on the given channel
   *
   * @param channel to retrieve old messages on
   * @return a {@link Completable} the will complete once the catchup is finished
   */
  Completable catchup(String channel);

  /**
   * Publish some {@link Data} on the given channel
   *
   * @param keyPair used to sign the sent message
   * @param channel to send the data on
   * @param data to send
   * @return a {@link Completable} the will complete once the publish is finished
   */
  Completable publish(KeyPair keyPair, String channel, Data data);

  /**
   * Subscribe to given channel
   *
   * <p>When the subscription is complete, the system will automatically catchup on that channel
   *
   * @param channel to subscribe to
   * @return a {@link Completable} the will complete once the subscription is finished
   */
  Completable subscribe(String channel);

  /**
   * Unsubscribe of given channel
   *
   * @param channel to unsubscribe from
   * @return a {@link Completable} the will complete once the unsubscribe is finished
   */
  Completable unsubscribe(String channel);
}

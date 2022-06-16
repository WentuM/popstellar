import {
  addOnChannelSubscriptionHandlers,
  addOnChannelUnsubscriptionHandlers,
  getNetworkManager,
  subscribeToChannel,
} from 'core/network';
import { ActionType, MessageRegistry, ObjectType } from 'core/network/jsonrpc/messages';
import { getStore } from 'core/redux';

import { getLaoById } from '../functions/lao';
import { addSubscribedChannel, removeSubscribedChannel, selectCurrentLaoId } from '../reducer';
import { storeBackendAndConnectToPeers, makeLaoGreetStoreWatcher } from './LaoGreetWatcher';
import {
  handleLaoCreateMessage,
  handleLaoGreetMessage,
  handleLaoStateMessage,
  handleLaoUpdatePropertiesMessage,
} from './LaoHandler';
import { CreateLao, StateLao, UpdateLao } from './messages';
import { GreetLao } from './messages/GreetLao';

export * from './LaoMessageApi';

/**
 * Configures the network callbacks in a MessageRegistry.
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configureNetwork(registry: MessageRegistry) {
  registry.add(ObjectType.LAO, ActionType.CREATE, handleLaoCreateMessage, CreateLao.fromJson);
  registry.add(ObjectType.LAO, ActionType.STATE, handleLaoStateMessage, StateLao.fromJson);
  registry.add(
    ObjectType.LAO,
    ActionType.UPDATE_PROPERTIES,
    handleLaoUpdatePropertiesMessage,
    UpdateLao.fromJson,
  );
  registry.add(ObjectType.LAO, ActionType.GREET, handleLaoGreetMessage, GreetLao.fromJson);

  // the lao#greet message can become valid after receiving it if the signature is only added
  // afterwards. listen to state changes that add signatures to lao#greet messages
  const store = getStore();
  store.subscribe(makeLaoGreetStoreWatcher(store, storeBackendAndConnectToPeers));

  // Workaround for https://github.com/dedis/popstellar/issues/1078
  addOnChannelSubscriptionHandlers((laoId, dispatch, channel) =>
    dispatch(addSubscribedChannel(laoId, channel)),
  );

  addOnChannelUnsubscriptionHandlers((laoId, dispatch, channel) =>
    dispatch(removeSubscribedChannel(laoId, channel)),
  );

  // in case of a reconnection, subscribe to and catchup on the LAO channel
  getNetworkManager().addReconnectionHandler(async () => {
    // after reconnecting, check whether we have already been connected to a LAO

    const laoId = selectCurrentLaoId(getStore().getState());
    const lao = getLaoById(laoId?.valueOf() || '');
    if (!laoId || !lao) {
      return;
    }

    await Promise.all(
      lao.subscribed_channels.map((channel) => subscribeToChannel(laoId, store.dispatch, channel)),
    );
  });
}

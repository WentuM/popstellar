import { KeyPairRegistry } from 'core/keypair/KeyPairRegistry';
import { setSignatureKeyPair } from 'core/network/JsonRpcApi';
import { addReducer, getStore } from 'core/redux';
import { getNetworkManager } from '../NetworkManager';
import { MessageRegistry } from '../jsonrpc/messages';
import { handleRpcRequests, setMessageRegistry } from './Handler';
import { makeMessageStoreWatcher } from './Watcher';
import messageReducer from './Reducer';

/**
 * Configures all handlers of the system within a MessageRegistry, and configures signatures
 * with a KeyPairRegistry.
 *
 * @param messageRegistry
 * @param keyPairRegistry
 */
export function configure(messageRegistry: MessageRegistry, keyPairRegistry: KeyPairRegistry) {
  // configure the message handlers
  setMessageRegistry(messageRegistry);
  setSignatureKeyPair(messageRegistry, keyPairRegistry);

  // configure the message reducer
  addReducer(messageReducer);

  // setup the handler for incoming messages
  getNetworkManager().setRpcHandler(handleRpcRequests);

  // returns the unsubscribe function, which we don't need.
  const store = getStore();
  store.subscribe(makeMessageStoreWatcher(store, messageRegistry));
}

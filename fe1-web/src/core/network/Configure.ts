import { KeyPairRegistry } from 'core/keypair';
import { MessageRegistry } from './jsonrpc/messages';
import { configureJsonRpcApi } from './JsonRpcApi';
import { configureIngestion } from './ingestion';
import { getNetworkManager } from './NetworkManager';
/**
 * Configures the network with a MessageRegistry and a KeyPairRegistry.
 *
 * @param messageRegistry
 * @param keyPairRegistry
 */
export function configureNetwork(
  messageRegistry: MessageRegistry,
  keyPairRegistry: KeyPairRegistry,
) {
  configureIngestion(messageRegistry, (h) => getNetworkManager().setRpcHandler(h));
  configureJsonRpcApi(messageRegistry, keyPairRegistry);
}
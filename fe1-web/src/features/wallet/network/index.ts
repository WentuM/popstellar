import { ActionType, MessageRegistry, ObjectType } from 'core/network/jsonrpc/messages';

import { PostTransaction } from './messages/PostTransaction';

/**
 * Configures the network callbacks in a MessageRegistry.
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configureNetwork(registry: MessageRegistry) {
  registry.add(ObjectType.TRANSACTION, ActionType.POST, () => true, PostTransaction.fromJson);
}

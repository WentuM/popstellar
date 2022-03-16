import { MessageRegistry } from 'core/network/jsonrpc/messages';

import { configureNetwork } from './network';

/**
 * Configures the meeting feature
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configure(registry: MessageRegistry) {
  configureNetwork(registry);
}
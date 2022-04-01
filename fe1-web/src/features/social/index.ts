import { MessageRegistry } from 'core/network/jsonrpc/messages';

import * as navigation from './navigation';
import { configureNetwork } from './network';
import { socialReducer } from './reducer';

/**
 * Configures the social media feature
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configure(registry: MessageRegistry) {
  configureNetwork(registry);
  return {
    navigation,
    reducers: {
      ...socialReducer,
    },
  };
}

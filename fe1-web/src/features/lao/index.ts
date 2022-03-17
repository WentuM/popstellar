import { MessageRegistry } from 'core/network/jsonrpc/messages';
import { configureNetwork } from './network';
import { PublicComponents } from './components';
import * as hooks from './hooks';
import * as functions from './functions';
import * as navigation from './navigation';
import { laoReducer } from './reducer';

/**
 * Configures the LAO feature
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configure(registry: MessageRegistry) {
  configureNetwork(registry);

  return {
    components: PublicComponents,
    hooks,
    functions,
    navigation,
    reducers: {
      ...laoReducer,
    },
  };
}

import { ActionType, MessageRegistry, ObjectType } from 'core/network/jsonrpc/messages';
import { Hash } from 'core/objects';
import { dispatch } from 'core/redux';

import { Transaction } from '../objects/transaction';
import { addTransaction } from '../reducer';
import { handleTransactionPost } from './DigitalCashHandler';
import { PostTransaction } from './messages';

export * from './DigitalCashMessageApi';

/**
 * Configures the network callbacks in a MessageRegistry.
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configureNetwork(registry: MessageRegistry) {
  const addTransactionToState = (laoId: Hash, rcId: Hash, transaction: Transaction) => {
    dispatch(
      addTransaction({
        laoId: laoId.valueOf(),
        rollCallId: rcId.valueOf(),
        transactionMessage: transaction.toState(),
      }),
    );
  };

  registry.add(
    ObjectType.COIN,
    ActionType.POST_TRANSACTION,
    handleTransactionPost(addTransactionToState),
    PostTransaction.fromJSON,
  );
}

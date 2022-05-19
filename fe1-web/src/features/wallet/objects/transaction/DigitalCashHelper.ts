import { Hash } from 'core/objects';

import { TransactionState } from './Transaction';
import { TransactionInputState } from './TransactionInput';
import { TransactionOutputState } from './TransactionOutput';

/**
 * Get the total value out that corresponds to this public key hash from an array of transactions
 * @param pkHash the public key hash
 * @param transactions the transaction messages from which the amount out
 * @return the total value out
 */
export const getTotalValue = (pkHash: string | Hash, transactions: TransactionState[]): number => {
  const outputs = transactions.flatMap((tr) =>
    tr.outputs.filter((output) => output.script.publicKeyHash.valueOf() === pkHash.valueOf()),
  );
  return outputs.reduce((total, current) => total + current.value, 0);
};

/**
 * Constructs a partial Input object from transaction messages to take as input
 * @param pk the public key of the sender
 * @param transactions the transaction messages used as inputs
 */
export const getInputsInToSign = (
  pk: string,
  transactions: TransactionState[],
): Omit<TransactionInputState, 'script'>[] => {
  return transactions.flatMap((tr) =>
    tr.outputs
      .filter((output) => output.script.publicKeyHash.valueOf() === Hash.fromString(pk).valueOf())
      .map((output, index) => {
        return {
          txOutHash: tr.transactionId!.valueOf(),
          txOutIndex: index,
        };
      }),
  );
};

/**
 * Concatenates the partial inputs and the outputs in a string to sign over it by following the digital cash specification
 * @param inputs
 * @param outputs
 */
export const concatenateTxData = (
  inputs: Omit<TransactionInputState, 'script'>[],
  outputs: TransactionOutputState[],
) => {
  const inputsDataString = inputs.reduce(
    (dataString, input) => dataString + input.txOutHash!.valueOf() + input.txOutIndex!.toString(),
    '',
  );
  return outputs.reduce(
    (dataString, output) =>
      dataString +
      output.value.toString() +
      output.script.type +
      output.script.publicKeyHash.valueOf(),
    inputsDataString,
  );
};

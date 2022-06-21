import 'jest-extended';

import { mockKeyPair, mockLaoId } from '__tests__/utils';
import { getDigitalCashState } from 'features/digital-cash/reducer';

import {
  mockCBHash,
  mockCoinbaseTransactionJSON,
  mockKPHash,
  mockTransactionValue,
} from '../../__tests__/utils';
import { Transaction } from '../../objects/transaction';
import { DigitalCashStore } from '../DigitalCashStore';

const mockTransaction = Transaction.fromJSON(mockCoinbaseTransactionJSON, mockCBHash).toState();

const filledState = {
  byLaoId: {
    [mockLaoId.valueOf()]: {
      balances: {
        [mockKPHash.valueOf()]: mockTransactionValue,
      },
      allTransactionsHash: [mockTransaction.transactionId],
      transactionsByHash: {
        [mockTransaction.transactionId!]: mockTransaction,
      },
      transactionsByPubHash: {
        [mockKPHash.valueOf()]: [mockTransaction.transactionId],
      },
    },
  },
};

jest.mock('features/digital-cash/reducer/DigitalCashReducer');

(getDigitalCashState as jest.Mock).mockReturnValue(filledState);

describe('Digital Cash Store', () => {
  it('should be able to recover a balance', () => {
    expect(DigitalCashStore.getBalance(mockLaoId, mockKeyPair.publicKey.valueOf())).toEqual(
      mockTransactionValue,
    );
  });

  it('should recover 0 as a balance if public key is not found', () => {
    expect(DigitalCashStore.getBalance(mockLaoId, 'pk')).toEqual(0);
  });

  it('should recover 0 as a balance if lao id is not found', () => {
    expect(DigitalCashStore.getBalance('mockId', mockKeyPair.publicKey.valueOf())).toEqual(0);
  });

  it('should recover the transaction by public key correctly', () => {
    expect(
      DigitalCashStore.getTransactionsByPublicKey(
        mockLaoId.valueOf(),
        mockKeyPair.publicKey.valueOf(),
      ),
    ).toEqual([mockTransaction]);
  });

  it('should recover an empty array if lao id is not found', () => {
    expect(
      DigitalCashStore.getTransactionsByPublicKey('mockId', mockKeyPair.publicKey.valueOf()),
    ).toEqual([]);
  });

  it('should recover an empty array if public key is not found', () => {
    expect(DigitalCashStore.getTransactionsByPublicKey('mockId', 'publicKey')).toEqual([]);
  });
});
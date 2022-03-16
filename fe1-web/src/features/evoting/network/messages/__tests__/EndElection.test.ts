import 'jest-extended';
import '__tests__/utils/matchers';
import {
  mockLao,
  mockLaoId,
  mockLaoIdHash,
  mockLaoName,
  configureTestFeatures,
} from '__tests__/utils';

import { Hash, Timestamp, ProtocolError, EventTags } from 'core/objects';
import { ActionType, ObjectType } from 'core/network/jsonrpc/messages';
import { OpenedLaoStore } from 'features/lao/store';

import { MessageDataProperties } from 'core/types';
import { Election, ElectionStatus, Question } from 'features/evoting/objects';
import STRINGS from 'resources/strings';
import { EndElection } from '../EndElection';

// region test data initialization

const TIMESTAMP = new Timestamp(1609455600); // 1st january 2021
const CLOSE_TIMESTAMP = new Timestamp(1609542000); // 2nd january 2021

const mockElectionId = Hash.fromStringArray(
  'Election',
  mockLaoId,
  TIMESTAMP.toString(),
  mockLaoName,
);

const mockQuestion = 'Mock Question 1';
const mockQuestionId = Hash.fromStringArray(
  EventTags.QUESTION,
  mockElectionId.toString(),
  mockQuestion,
);
const mockBallotOptions = ['Ballot Option 1', 'Ballot Option 2'];

const question: Question = {
  id: mockQuestionId.toString(),
  question: mockQuestion,
  voting_method: STRINGS.election_method_Plurality,
  ballot_options: mockBallotOptions,
  write_in: false,
};

const election = new Election({
  lao: mockLaoIdHash,
  id: mockElectionId,
  name: 'An election',
  version: STRINGS.election_version_identifier,
  createdAt: TIMESTAMP,
  start: TIMESTAMP,
  end: CLOSE_TIMESTAMP,
  questions: [question],
  electionStatus: ElectionStatus.NOT_STARTED,
  registeredVotes: [
    {
      createdAt: 0,
      messageId: '0',
      sender: '',
      votes: [
        { id: 'id1', question: 'q1', vote: [0] },
        { id: 'id2', question: 'q2', vote: [0] },
      ],
    },
    {
      createdAt: 1,
      messageId: '1',
      sender: '',
      votes: [
        { id: 'id3', question: 'q3', vote: [0] },
        { id: 'id4', question: 'q4', vote: [0] },
      ],
    },
  ],
});

const mockVoteId = 'x';
const mockElectionResultHash = Hash.fromStringArray(mockVoteId);

const electionId: Hash = Hash.fromStringArray(
  'Election',
  mockLaoId,
  TIMESTAMP.toString(),
  mockLaoName,
);

// In these tests, we should assume that the input to the messages is
// just a Partial<> and not a MessageDataProperties<>
// as this will catch more issues at runtime. (Defensive programming)
const sampleEndElection: Partial<EndElection> = {
  object: ObjectType.ELECTION,
  action: ActionType.END,
  election: electionId,
  lao: mockLaoIdHash,
  created_at: TIMESTAMP,
  registered_votes: mockElectionResultHash,
};

const endElectionJson: string = `{
  "object": "${ObjectType.ELECTION}",
  "action": "${ActionType.END}",
  "election": "${electionId}",
  "lao": "${mockLaoIdHash}",
  "created_at": ${TIMESTAMP},
  "registered_votes": "${mockElectionResultHash}"
}`;

// endregion

beforeAll(() => {
  configureTestFeatures();
  OpenedLaoStore.store(mockLao);
});

describe('EndElection', () => {
  it('should be created correctly from Json', () => {
    expect(new EndElection(sampleEndElection as MessageDataProperties<EndElection>)).toBeJsonEqual(
      sampleEndElection,
    );
    const temp = {
      object: ObjectType.ELECTION,
      action: ActionType.END,
      election: electionId,
      lao: mockLaoIdHash,
      created_at: TIMESTAMP,
      registered_votes: mockElectionResultHash,
    };
    expect(new EndElection(temp)).toBeJsonEqual(temp);
  });

  it('should be parsed correctly from Json', () => {
    const obj = JSON.parse(endElectionJson);
    expect(EndElection.fromJson(obj)).toBeJsonEqual(sampleEndElection);
  });

  it('fromJson should throw an error if the Json has incorrect action', () => {
    const obj = {
      object: ObjectType.ELECTION,
      action: ActionType.NOTIFY_ADD,
      election: electionId.toString(),
      lao: mockLaoIdHash.toString(),
      created_at: TIMESTAMP.valueOf(),
      registered_votes: mockElectionResultHash,
    };
    const createFromJson = () => EndElection.fromJson(obj);
    expect(createFromJson).toThrow(ProtocolError);
  });

  it('fromJson should throw an error if the Json has incorrect object', () => {
    const obj = {
      object: ObjectType.CHIRP,
      action: ActionType.OPEN,
      election: electionId.toString(),
      lao: mockLaoIdHash.toString(),
      created_at: TIMESTAMP.valueOf(),
      registered_votes: mockElectionResultHash,
    };
    const createFromJson = () => EndElection.fromJson(obj);
    expect(createFromJson).toThrow(ProtocolError);
  });

  describe('constructor', () => {
    it('should throw an error if election is undefined', () => {
      const createWrongObj = () =>
        new EndElection({
          lao: mockLaoIdHash,
          election: undefined as unknown as Hash,
          created_at: TIMESTAMP,
          registered_votes: mockElectionResultHash,
        });
      expect(createWrongObj).toThrow(ProtocolError);
    });

    it('should throw an error if lao is undefined', () => {
      const createWrongObj = () =>
        new EndElection({
          lao: undefined as unknown as Hash,
          election: electionId,
          created_at: TIMESTAMP,
          registered_votes: mockElectionResultHash,
        });
      expect(createWrongObj).toThrow(ProtocolError);
    });

    it('should throw an error if created_at is undefined', () => {
      const createWrongObj = () =>
        new EndElection({
          election: electionId,
          lao: mockLaoIdHash,
          created_at: undefined as unknown as Timestamp,
          registered_votes: mockElectionResultHash,
        });
      expect(createWrongObj).toThrow(ProtocolError);
    });

    it('should throw an error if registered_votes is undefined', () => {
      const createWrongObj = () =>
        new EndElection({
          election: electionId,
          lao: mockLaoIdHash,
          created_at: TIMESTAMP,
          registered_votes: undefined as unknown as Hash,
        });
      expect(createWrongObj).toThrow(ProtocolError);
    });

    it('should ignore passed object and action parameters', () => {
      const msg = new EndElection({
        object: ObjectType.CHIRP,
        action: ActionType.NOTIFY_ADD,
        election: electionId,
        lao: mockLaoIdHash,
        created_at: TIMESTAMP,
        registered_votes: mockElectionResultHash,
      } as MessageDataProperties<EndElection>);

      expect(msg.object).toEqual(ObjectType.ELECTION);
      expect(msg.action).toEqual(ActionType.END);
    });
  });

  describe('computeRegisteredVotesHash', () => {
    // It seems a bit silly to test this function apart from it not returning an error as
    // we would simply write the same code here again?
    const fn = () => EndElection.computeRegisteredVotesHash(election);
    expect(fn).not.toThrow();
    expect(fn().valueOf()).toEqual('eYH10agf4Jvfs-rihA-9pG1j0lFPHnYeI9e9Vx-GQ6Q=');
  });
});
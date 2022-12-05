import { ActionType, MessageData, ObjectType } from 'core/network/jsonrpc/messages';
import { validateDataObject } from 'core/network/validation';
import { checkTimestampStaleness } from 'core/network/validation/Checker';
import { EventTags, Hash, ProtocolError, Timestamp } from 'core/objects';
import { MessageDataProperties } from 'core/types';

import { ElectionVersion, Question } from '../../objects';

/** Data sent to setup an Election event */
export class SetupElection implements MessageData {
  public readonly object: ObjectType = ObjectType.ELECTION;

  public readonly action: ActionType = ActionType.SETUP;

  public readonly version: string;

  public readonly id: Hash;

  public readonly lao: Hash;

  public readonly name: string;

  public readonly created_at: Timestamp;

  public readonly start_time: Timestamp;

  public readonly end_time: Timestamp;

  public readonly questions: Question[];

  constructor(msg: MessageDataProperties<SetupElection>, laoId: Hash) {
    if (!msg.lao) {
      throw new ProtocolError("Undefined 'lao' parameter encountered during 'SetupElection'");
    }
    this.lao = msg.lao;

    if (!msg.version) {
      throw new ProtocolError("Undefined 'version' parameter encountered during 'SetupElection'");
    }
    if (
      ![ElectionVersion.OPEN_BALLOT, ElectionVersion.SECRET_BALLOT].includes(
        msg.version as ElectionVersion,
      )
    ) {
      throw new ProtocolError("Invalid 'version' parameter encountered during 'SetupElection'");
    }
    this.version = msg.version;

    if (!msg.id) {
      throw new ProtocolError("Undefined 'id' parameter encountered during 'SetupElection'");
    }
    this.id = msg.id;

    if (!msg.name) {
      throw new ProtocolError("Undefined 'name' parameter encountered during 'SetupElection'");
    }
    this.name = msg.name;

    if (!msg.created_at) {
      throw new ProtocolError(
        "Undefined 'created_at' parameter encountered during 'SetupElection'",
      );
    }
    checkTimestampStaleness(msg.created_at);
    this.created_at = msg.created_at;
    if (!msg.start_time) {
      throw new ProtocolError(
        "Undefined 'start_time' parameter encountered during 'SetupElection'",
      );
    }
    checkTimestampStaleness(msg.start_time);
    if (!msg.end_time) {
      throw new ProtocolError("Undefined 'end_time' parameter encountered during 'SetupElection'");
    }
    checkTimestampStaleness(msg.end_time);
    if (msg.start_time.before(msg.created_at)) {
      throw new ProtocolError(
        "Invalid timestamp encountered: 'start' parameter smaller than 'created_at'",
      );
    }
    this.start_time = msg.start_time;
    if (msg.end_time.before(msg.start_time)) {
      throw new ProtocolError(
        "Invalid timestamp encountered: 'end' parameter smaller than 'start'",
      );
    }
    this.end_time = msg.end_time;

    if (!msg.id) {
      throw new ProtocolError("Undefined 'id' parameter encountered during 'SetupElection'");
    }

    const expectedId = SetupElection.computeElectionId(laoId, this.created_at, this.name);
    if (!expectedId.equals(msg.id)) {
      throw new ProtocolError(
        "Invalid 'id' parameter encountered during 'SetupElection':" +
          ' re-computing the value yields a different result (' +
          `(expected: '${expectedId}', actual: '${msg.id}')`,
      );
    }

    if (!msg.questions) {
      throw new ProtocolError("Undefined 'questions' parameter encountered during 'SetupElection'");
    }
    SetupElection.validateQuestions(msg.questions, msg.id);
    this.questions = msg.questions;
  }

  /**
   * Computes the id of an election
   *
   * @param laoId - The ID of the LAO this message was sent to
   * @param createdAt - The time the election was created
   * @param name The name of the election
   */
  public static computeElectionId(laoId: Hash, createdAt: Timestamp, name: string) {
    return Hash.fromArray(EventTags.ELECTION, laoId, createdAt, name);
  }

  /**
   * Checks that an array of questions is valid.
   *
   * @param questions - The array of questions to be checked
   * @param electionId - The id of the election
   */
  public static validateQuestions(questions: Question[], electionId: Hash) {
    questions.forEach((question) => {
      const expectedHash = Hash.fromArray(EventTags.QUESTION, electionId, question.question);

      if (!expectedHash.equals(question.id)) {
        throw new ProtocolError(
          "Invalid 'questions.id' parameter encountered during 'SetupElection':" +
            ' re-computing the value yields a different result',
        );
      }
      if (!question.id) {
        throw new ProtocolError(
          "Undefined 'question id' parameter encountered during 'SetupElection'",
        );
      }
      if (!question.question) {
        throw new ProtocolError(
          "Undefined 'question' parameter encountered during 'SetupElection'",
        );
      }
      if (!question.voting_method) {
        throw new ProtocolError(
          "Undefined 'voting method' parameter encountered during 'SetupElection'",
        );
      }
      if (!question.ballot_options) {
        throw new ProtocolError(
          "Undefined 'ballot_options' parameter encountered during 'SetupElection'",
        );
      }
    });
  }

  /**
   * Creates a SetupElection object from a given object.
   *
   * @param obj The parsed json data
   * @param laoId The id of the lao this message belongs to
   */
  public static fromJson(obj: any, laoId?: Hash): SetupElection {
    if (!laoId) {
      throw new Error(
        "Tried build a 'SetupElection' message without knowing the associated lao id",
      );
    }

    const { errors } = validateDataObject(ObjectType.ELECTION, ActionType.SETUP, obj);

    if (errors !== null) {
      throw new ProtocolError(`Invalid election setup\n\n${errors}`);
    }

    return new SetupElection(
      {
        id: new Hash(obj.id),
        lao: new Hash(obj.lao),
        created_at: new Timestamp(obj.created_at),
        start_time: new Timestamp(obj.start_time),
        end_time: new Timestamp(obj.end_time),
        name: obj.name,
        questions: obj.questions.map(Question.fromJson),
        version: obj.version,
      },
      laoId,
    );
  }
}

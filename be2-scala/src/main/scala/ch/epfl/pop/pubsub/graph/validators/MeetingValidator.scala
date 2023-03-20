package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.ObjectType
import ch.epfl.pop.model.network.method.message.data.meeting.{CreateMeeting, StateMeeting}
import ch.epfl.pop.model.objects.{Channel, Hash, PublicKey}
import ch.epfl.pop.pubsub.graph.validators.MessageValidator._
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}
import akka.pattern.AskableActorRef
import ch.epfl.pop.storage.DbActor
import ch.epfl.pop.storage.DbActor.DbActorReadRollCallDataAck

object MeetingValidator extends MessageDataContentValidator with EventValidator {

  val meetingValidator = new MeetingValidator(DbActor.getInstance)

  override val EVENT_HASH_PREFIX: String = meetingValidator.EVENT_HASH_PREFIX

  def validateCreateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = meetingValidator.validateCreateMeeting(rpcMessage)

  def validateStateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = meetingValidator.validateStateMeeting(rpcMessage)

}

sealed class MeetingValidator(dbActorRef: => AskableActorRef) extends MessageDataContentValidator with EventValidator {
  override val EVENT_HASH_PREFIX: String = "M"

  def validateCreateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "CreateMeeting", rpcMessage.id)

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: CreateMeeting = message.decodedData.get.asInstanceOf[CreateMeeting]

        val laoId: Hash = rpcMessage.extractLaoId
        val expectedHash: Hash = Hash.fromStrings(EVENT_HASH_PREFIX, laoId.toString, data.creation.toString, data.name)

        val sender: PublicKey = message.sender
        val channel: Channel = rpcMessage.getParamsChannel

        if (!validateTimestampStaleness(data.creation)) {
          Left(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateTimestampStaleness(data.start)) {
          Left(validationError(s"stale 'start' timestamp (${data.start})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.creation, data.end.get)) {
          Left(validationError(s"'end' (${data.end.get}) timestamp is smaller than 'creation' (${data.creation})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.start, data.end.get)) {
          Left(validationError(s"'end' (${data.end.get}) timestamp is smaller than 'start' (${data.start})"))
        } else if (expectedHash != data.id) {
          Left(validationError("unexpected id"))
        } else if (!validateOwner(sender, channel, dbActorRef)) {
          Left(validationError(s"invalid sender $sender"))
        } else if (!validateChannelType(ObjectType.LAO, channel, dbActorRef)) {
          Left(validationError(s"trying to send an CreateMeeting message on a wrong type of channel $channel"))
        } else {
          Right(rpcMessage)
        }
      case _ => Left(validationErrorNoMessage(rpcMessage.id))
    }
  }

  def validateStateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "StateMeeting", rpcMessage.id)

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: StateMeeting = message.decodedData.get.asInstanceOf[StateMeeting]

        val laoId: Hash = rpcMessage.extractLaoId
        val expectedHash: Hash = Hash.fromStrings(EVENT_HASH_PREFIX, laoId.toString, data.creation.toString, data.name)

        if (!validateTimestampStaleness(data.creation)) {
          Left(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateTimestampOrder(data.creation, data.last_modified)) {
          Left(validationError(s"'last_modified' (${data.last_modified}) timestamp is smaller than 'creation' (${data.creation})"))
        } else if (!validateTimestampStaleness(data.start)) {
          Left(validationError(s"stale 'start' timestamp (${data.start})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.creation, data.end.get)) {
          Left(validationError(s"'end' (${data.end.get}) timestamp is smaller than 'creation' (${data.creation})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.start, data.end.get)) {
          Left(validationError(s"'end' (${data.end.get}) timestamp is smaller than 'start' (${data.start})"))
        } else if (!validateWitnessSignatures(data.modification_signatures, data.modification_id)) {
          Left(validationError("witness key-signature pairs are not valid for the given modification_id"))
        } else if (expectedHash != data.id) {
          Left(validationError("unexpected id"))
        } else {
          Right(rpcMessage)
        }
      case _ => Left(validationErrorNoMessage(rpcMessage.id))
    }
  }
}

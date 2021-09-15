package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.lao.{CreateLao, StateLao, UpdateLao}
import ch.epfl.pop.model.objects.{Channel, Hash}
import ch.epfl.pop.pubsub.graph.{DbActor, ErrorCodes, GraphMessage, PipelineError}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}


case object LaoValidator extends MessageDataContentValidator {
  def validateCreateLao(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "CreateLao")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: CreateLao = message.decodedData.get.asInstanceOf[CreateLao]
        val expectedHash: Hash = Hash.fromStrings(data.organizer.base64Data.decode(), data.creation.toString, data.name)

        if (!validateTimestampStaleness(data.creation)) {
          Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateWitnesses(data.witnesses)) {
          Right(validationError("duplicate witnesses keys"))
        /* FIXME hash issues
        } else if (expectedHash != data.id) {
          Right(validationError("unexpected id"))
        */
        } else if (data.organizer != message.sender) {
          Right(validationError("unexpected organizer public key"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }

  def validateStateLao(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "StateLao")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: StateLao = message.decodedData.get.asInstanceOf[StateLao]

        // FIXME get lao creation message in order to calculate expected hash
        val f: Future[GraphMessage] = (dbActor ? DbActor.Read(Channel.rootChannel, ???)).map {
          case DbActor.DbActorReadAck(Some(retrievedMessage)) =>
            val laoCreationMessage = retrievedMessage.decodedData.get.asInstanceOf[CreateLao]
            // Calculate expected hash
            val expectedHash: Hash = Hash.fromStrings(
              retrievedMessage.sender.toString, laoCreationMessage.creation.toString, laoCreationMessage.name
            )

            if (!validateTimestampStaleness(data.creation)) {
              Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
            } else if (!validateTimestampOrder(data.creation, data.last_modified)) {
              Right(validationError(s"'last_modified' (${data.last_modified}) timestamp is younger than 'creation' (${data.creation})"))
            } else if (!validateWitnesses(data.witnesses)) {
              Right(validationError("duplicate witnesses keys"))
            } else if (!validateWitnessSignatures(data.modification_signatures, data.modification_id)) {
              Right(validationError("witness key-signature pairs are not valid for the given modification_id"))
            } else if (expectedHash != data.id) {
              Right(validationError("unexpected id"))
            } else {
              Left(rpcMessage)
            }

          case DbActor.DbActorReadAck(None) =>
            Right(PipelineError(ErrorCodes.INVALID_RESOURCE.id, "No CreateLao message associated found"))
          case DbActor.DbActorNAck(code, description) =>
            Right(PipelineError(code, description))
          case _ =>
            Right(PipelineError(ErrorCodes.SERVER_ERROR.id, "Database actor returned an unknown answer"))
        }

        Await.result(f, duration)

      case _ => Right(validationErrorNoMessage)
    }
  }

  def validateUpdateLao(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "UpdateLao")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: UpdateLao = message.decodedData.get.asInstanceOf[UpdateLao]

        // FIXME get lao creation message in order to calculate "SHA256(organizer||creation||name)"
        val f: Future[GraphMessage] = (dbActor ? DbActor.Read(Channel.rootChannel, ???)).map {
          case DbActor.DbActorReadAck(Some(retrievedMessage)) =>
            val laoCreationMessage = retrievedMessage.decodedData.get.asInstanceOf[CreateLao]
            // Calculate expected hash
            val expectedHash: Hash = Hash.fromStrings(
              retrievedMessage.sender.toString, laoCreationMessage.creation.toString, laoCreationMessage.name
            )

            if (!validateTimestampStaleness(data.last_modified)) {
              Right(validationError(s"stale 'last_modified' timestamp (${data.last_modified})"))
            } else if (!validateWitnesses(data.witnesses)) {
              Right(validationError("duplicate witnesses keys"))
            } else if (expectedHash != data.id) {
              Right(validationError("unexpected id"))
            } else {
              Left(rpcMessage)
            }

          case DbActor.DbActorReadAck(None) =>
            Right(PipelineError(ErrorCodes.INVALID_RESOURCE.id, "No CreateLao message associated found"))
          case DbActor.DbActorNAck(code, description) =>
            Right(PipelineError(code, description))
          case _ =>
            Right(PipelineError(ErrorCodes.SERVER_ERROR.id, "Database actor returned an unknown answer"))
        }

        Await.result(f, duration)

      case _ => Right(validationErrorNoMessage)
    }
  }
}
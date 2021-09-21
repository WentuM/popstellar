package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.{JsonRpcRequest, JsonRpcResponse}
import ch.epfl.pop.model.network.requests.rollCall.{JsonRpcRequestCloseRollCall, JsonRpcRequestCreateRollCall, JsonRpcRequestOpenRollCall, JsonRpcRequestReopenRollCall}
import ch.epfl.pop.pubsub.graph.{DbActor, ErrorCodes, GraphMessage, PipelineError}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

case object RollCallHandler extends MessageHandler {

  override val handler: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    case Left(jsonRpcMessage) => jsonRpcMessage match {
      case message@(_: JsonRpcRequestCreateRollCall) => handleCreateRollCall(message)
      case message@(_: JsonRpcRequestOpenRollCall) => handleOpenRollCall(message)
      case message@(_: JsonRpcRequestReopenRollCall) => handleReopenRollCall(message)
      case message@(_: JsonRpcRequestCloseRollCall) => handleCloseRollCall(message)
      case _ => Right(PipelineError(
        ErrorCodes.SERVER_ERROR.id,
        "Internal server fault: RollCallHandler was given a message it could not recognize",
        jsonRpcMessage match {
          case r: JsonRpcRequest => r.id
          case r: JsonRpcResponse => r.id
          case _ => None
        }
      ))
    }
    case graphMessage@_ => graphMessage
  }

  def handleCreateRollCall(rpcMessage: JsonRpcRequest): GraphMessage = {
    val message: Message = rpcMessage.getParamsMessage.get

    val f: Future[GraphMessage] = (dbActor ? DbActor.Write(rpcMessage.getParamsChannel, message)).map {
      case DbActor.DbActorWriteAck => Left(rpcMessage)
      case DbActor.DbActorNAck(code, description) => Right(PipelineError(code, description, rpcMessage.id))
      case _ => Right(PipelineError(ErrorCodes.SERVER_ERROR.id, "Database actor returned an unknown answer", rpcMessage.id))
    }

    Await.result(f, duration)
  }
  def handleOpenRollCall(rpcMessage: JsonRpcRequest): GraphMessage = dbAskWritePropagate(rpcMessage)
  def handleReopenRollCall(rpcMessage: JsonRpcRequest): GraphMessage = dbAskWritePropagate(rpcMessage)
  def handleCloseRollCall(rpcMessage: JsonRpcRequest): GraphMessage = dbAskWritePropagate(rpcMessage)
}

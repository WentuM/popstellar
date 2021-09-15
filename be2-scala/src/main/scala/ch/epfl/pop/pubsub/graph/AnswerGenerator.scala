package ch.epfl.pop.pubsub.graph

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import ch.epfl.pop.model.network._
import ch.epfl.pop.pubsub.graph.validators.RpcValidator
import ch.epfl.pop.model.network.ResultObject
import ch.epfl.pop.model.network.method.{Broadcast, Catchup}
import ch.epfl.pop.model.network.method.message.Message

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object AnswerGenerator {

  def generateAnswer(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    // Note: the output message (if successful) is an answer
    // The standard output is always a JsonMessage (pipeline errors are transformed into negative answers)

    case Left(rpcRequest: JsonRpcRequest) => rpcRequest.getParams match {
      case Catchup(channel) =>
        implicit val timeout: Timeout = DbActor.getTimeout

        val f: Future[GraphMessage] = (DbActor.getInstance ? DbActor.Catchup(channel)).map {
          case DbActor.DbActorCatchupAck(list: List[Message]) =>
            val resultObject: ResultObject = new ResultObject(list)
            Left(JsonRpcResponse(RpcValidator.JSON_RPC_VERSION, Some(resultObject), None, rpcRequest.id))
          case DbActor.DbActorNAck(code, description) => Right(PipelineError(code, description))
          case _ => Right(PipelineError(ErrorCodes.SERVER_ERROR.id, "Database actor returned an unknown answer"))
        }

        Await.result(f, DbActor.getDuration)


      case Broadcast(_, _) => ???

      // Standard answer res == 0
      case _ => Left(JsonRpcResponse(
        RpcValidator.JSON_RPC_VERSION, Some(new ResultObject(0)), None, rpcRequest.id
      ))
    }

    // TODO null id
    case Right(pipelineError: PipelineError) => Left(JsonRpcResponse(
      RpcValidator.JSON_RPC_VERSION,
      None,
      Some(ErrorObject(pipelineError.code, pipelineError.description)),
      None
    ))

    // /!\ If something is outputted as Right(...), then there's a mistake somewhere in the graph!
    case _ => Right(PipelineError(
      ErrorCodes.SERVER_ERROR.id,
      s"Internal server error: unknown reason. The MessageEncoder could not decide what to do with input $graphMessage"
    ))
  }

  val generator: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map(generateAnswer)
}
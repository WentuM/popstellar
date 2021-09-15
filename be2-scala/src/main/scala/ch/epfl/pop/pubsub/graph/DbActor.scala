package ch.epfl.pop.pubsub.graph

import java.util.concurrent.TimeUnit
import java.io.File

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.pattern.AskableActorRef
import akka.util.Timeout
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.objects.{Channel, Hash}
import ch.epfl.pop.pubsub.PublishSubscribe
import org.iq80.leveldb.impl.Iq80DBFactory.factory
import org.iq80.leveldb.{DB, DBIterator, Options}

import scala.collection.mutable
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}

object DbActor {

  final val DATABASE_FOLDER: String = "database"
  final val DURATION: FiniteDuration = Duration(1, TimeUnit.SECONDS)
  final val TIMEOUT: Timeout = Timeout(DURATION)
  final lazy val INSTANCE: AskableActorRef = PublishSubscribe.getDbActorRef

  // DbActor Events correspond to messages the actor may receive
  sealed trait Event

  /**
   * Request to write a message in the database
   * @param channel the channel where the message must be published
   * @param message the message to write in the database
   */
  final case class Write(channel: Channel, message: Message) extends Event

  /**
   * Request to read a specific messages on a channel
   * @param channel the channel where the message was published
   * @param id the id of the message (message_id) we want to read
   */
  final case class Read(channel: Channel, id: Hash) extends Event

  /**
   * Request to read all messages from a specific channel
   * @param channel the channel where the messages should be fetched
   */
  final case class Catchup(channel: Channel) extends Event

  final case class AddWitnessSignature() extends Event // TODO add sig to a message


  // DbActor Events correspond to messages the actor may emit
  sealed trait DbActorMessage

  /**
   * Response for a [[Write]] db request
   * Receiving [[DbActorWriteAck]] works as an acknowledgement that the write request was successful
   */
  final case object DbActorWriteAck extends DbActorMessage

  /**
   * Response for a [[Read]] db request
   * Receiving [[DbActorReadAck]] works as an acknowledgement that the read request was successful
   *
   * @param message requested message
   */
  final case class DbActorReadAck(message: Option[Message]) extends DbActorMessage

  /**
   * Response for a [[Catchup]] db request
   * Receiving [[DbActorCatchupAck]] works as an acknowledgement that the catchup request was successful
   *
   * @param messages requested messages
   */
  final case class DbActorCatchupAck(messages: List[Message]) extends DbActorMessage

  /**
   * Response for a negative db request
   *
   * @param code error code corresponding to the error encountered
   * @param description description of the error encountered
   */
  final case class DbActorNAck(code: Int, description: String) extends DbActorMessage


  def getInstance: AskableActorRef = INSTANCE

  def getTimeout: Timeout = TIMEOUT

  def getDuration: FiniteDuration = DURATION

  def apply(): DbActor = DbActor()

  sealed case class DbActor() extends Actor with ActorLogging {
    private val channelsMap: mutable.Map[Channel, DB] = mutable.Map.empty

    override def receive: Receive = LoggingReceive {
      case Write(channel, message) =>
        log.info(s"Actor $self (db) received a WRITE request on channel '${channel.toString}'")

        val channelDb: DB = channelsMap.get(channel) match {
          case Some(channelDb) => channelDb
          case _ =>
            val options = new Options()
            options.createIfMissing(true)
            val channelDb = factory.open(new File(s"$DATABASE_FOLDER/${channel.toString}"), options)
            channelsMap += (channel -> channelDb)
            channelDb
        }

        val messageId: Hash = message.message_id
        Try(channelDb.put(messageId.getBytes, message.toJsonString.getBytes)) match {
          case Success(_) =>
            log.info(s"Actor $self (db) wrote message_id '$messageId' on channel '$channel'")
            sender ! DbActorWriteAck
          case Failure(exception) =>
            log.info(s"Actor $self (db) encountered a problem while writing message_id '$messageId' on channel '$channel'")
            sender ! DbActorNAck(ErrorCodes.SERVER_ERROR.id, exception.getMessage)
        }

      case Read(channel, messageId) =>
        log.info(s"Actor $self (db) received a READ request for message_id '$messageId' from channel '$channel'")

        if (channelsMap.contains(channel)) {
          val channelDb: DB = channelsMap(channel)
          Try(channelDb.get(messageId.getBytes)) match {
            case Success(bytes) if bytes != null =>
              sender ! DbActorReadAck(Some(Message.buildFromJson(bytes.map(_.toChar).mkString)))
            case _ =>
              sender ! DbActorNAck(ErrorCodes.SERVER_ERROR.id, "Unknown read database error")
          }
        } else {
          sender ! DbActorReadAck(None)
        }

      case Catchup(channel) =>
        log.info(s"Actor $self (db) received a CATCHUP request for channel '$channel'")

        @scala.annotation.tailrec
        def buildCatchupList(iterator: DBIterator, acc: List[Message]): List[Message] = {
          if (iterator.hasNext) {
            val value: Message = Message.buildFromJson(iterator.next().getValue.map(_.toChar).mkString)
            buildCatchupList(iterator, value :: acc)
          } else {
            acc
          }
        }

        if (channelsMap.contains(channel)) {
          val iterator: DBIterator = channelsMap(channel).iterator

          iterator.seekToFirst()
          val result: List[Message] = buildCatchupList(iterator, List.empty).reverse
          iterator.close()

          sender ! DbActorCatchupAck(result)

        } else {
          sender ! DbActorNAck(
            ErrorCodes.INVALID_RESOURCE.id,
            "Database cannot catchup from a channel that does not exist in db"
          )
        }


      case m@_ =>
        log.info(s"Actor $self (db) received an unknown message")
        sender ! DbActorNAck(ErrorCodes.SERVER_ERROR.id, s"database actor received a message '$m' that it could not recognize")
    }
  }
}
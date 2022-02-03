package ch.epfl.pop.pubsub.graph.validators

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.AskableActorRef
import akka.testkit.{ImplicitSender,TestKit,TestProbe}
import akka.util.Timeout

import ch.epfl.pop.model.objects.{Base64Data, Channel, ChannelData, LaoData, PrivateKey, PublicKey}
import ch.epfl.pop.model.network.method.message.data.ObjectType
import ch.epfl.pop.pubsub.{AskPatternConstants, PubSubMediator}
import ch.epfl.pop.pubsub.graph.{DbActor, ErrorCodes, GraphMessage, PipelineError}

import util.examples.JsonRpcRequestExample._
import util.examples.socialMedia.AddChirpExamples

import org.scalatest.{BeforeAndAfterAll,FunSuiteLike,Matchers}

import scala.concurrent.duration.FiniteDuration

import scala.reflect.io.Directory
import java.io.File

import java.util.concurrent.TimeUnit

import scala.concurrent.Await

class SocialMediaValidatorSuite extends TestKit(ActorSystem("socialMediaValidatorTestActorSystem"))
    with FunSuiteLike
    with ImplicitSender
    with Matchers with BeforeAndAfterAll with AskPatternConstants {

    final val DB_TEST_FOLDER: String = "databaseSocialMediaTest"

    val pubSubMediatorRef: ActorRef = system.actorOf(PubSubMediator.props, "PubSubMediator")
    val dbActorRef: AskableActorRef = system.actorOf(Props(DbActor(pubSubMediatorRef, DB_TEST_FOLDER)), "DbActor")

    // Implicit for system actors
    implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)
    
    override def afterAll(): Unit = {
        // Stops the testKit
        TestKit.shutdownActorSystem(system)

        // Deletes the test database
        val directory = new Directory(new File(DB_TEST_FOLDER))
        directory.deleteRecursively()
    }

    private final val PUBLICKEY: PublicKey = PublicKey(Base64Data("jsNj23IHALvppqV1xQfP71_3IyAHzivxiCz236_zzQc="))
    private final val PRIVATEKEY: PrivateKey = PrivateKey(Base64Data("qRfms3wzSLkxAeBz6UtwA-L1qP0h8D9XI1FSvY68t7Y="))
    private final val PKOWNER: PublicKey = PublicKey(Base64Data.encode("owner"))
    private final val laoDataRight: LaoData = LaoData(PKOWNER, List(AddChirpExamples.SENDER_ADDCHIRP), PRIVATEKEY, PUBLICKEY, List.empty)
    private final val laoDataWrong: LaoData = LaoData(PKOWNER, List(PKOWNER), PRIVATEKEY, PUBLICKEY, List.empty)
    private final val channelDataRight: ChannelData = ChannelData(ObjectType.CHIRP, List.empty)
    private final val channelDataWrong: ChannelData = ChannelData(ObjectType.LAO, List.empty)
    private final val channelDataReaction: ChannelData = ChannelData(ObjectType.REACTION, List.empty)

    private def mockDbWorking: AskableActorRef = {
        val mockedDB = Props(new Actor(){
            override def receive = {
                case DbActor.ReadLaoData(channel) =>
                    sender ! DbActor.DbActorReadLaoDataAck(Some(laoDataRight))
                case DbActor.ReadChannelData(channel) =>
                    sender ! DbActor.DbActorReadChannelDataAck(Some(channelDataRight))
            }
        }
        )
        system.actorOf(mockedDB)
    }

    private def mockDbWorkingReaction: AskableActorRef = {
        val mockedDB = Props(new Actor(){
            override def receive = {
                case DbActor.ReadLaoData(channel) =>
                    sender ! DbActor.DbActorReadLaoDataAck(Some(laoDataRight))
                case DbActor.ReadChannelData(channel) =>
                    sender ! DbActor.DbActorReadChannelDataAck(Some(channelDataReaction))
            }
        }
        )
        system.actorOf(mockedDB)
    }

    private def mockDbWrongTokenReaction: AskableActorRef = {
        val mockedDB = Props(new Actor(){
            override def receive = {
                case DbActor.ReadLaoData(channel) =>
                    sender ! DbActor.DbActorReadLaoDataAck(Some(laoDataWrong))
                case DbActor.ReadChannelData(channel) =>
                    sender ! DbActor.DbActorReadChannelDataAck(Some(channelDataReaction))
            }
        }
        )
        system.actorOf(mockedDB)
    }

    private def mockDbWrongToken: AskableActorRef = {
        val mockedDB = Props(new Actor(){
            override def receive = {
                case DbActor.ReadLaoData(channel) =>
                    sender ! DbActor.DbActorReadLaoDataAck(Some(laoDataWrong))
                case DbActor.ReadChannelData(channel) =>
                    sender ! DbActor.DbActorReadChannelDataAck(Some(channelDataRight))
            }
        }
        )
        system.actorOf(mockedDB)
    }

    private def mockDbWrongChannel: AskableActorRef = {
        val mockedDB = Props(new Actor(){
            override def receive = {
                case DbActor.ReadLaoData(channel) =>
                    sender ! DbActor.DbActorReadLaoDataAck(Some(laoDataRight))
                case DbActor.ReadChannelData(channel) =>
                    sender ! DbActor.DbActorReadChannelDataAck(Some(channelDataWrong))
            }
        }
        )
        system.actorOf(mockedDB)
    }

    //AddChirp
    test("Adding a chirp works as intended"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(ADD_CHIRP_RPC)
        message should equal(Left(ADD_CHIRP_RPC))
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a chirp with too long text fails"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(ADD_CHIRP_WRONG_TEXT_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a chirp with invalid Timestamp fails"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(ADD_CHIRP_WRONG_TIMESTAMP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a chirp without valid PoP token fails"){
        val dbActorRef = mockDbWrongToken
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(ADD_CHIRP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a chirp on wrong type of channel fails"){
        val dbActorRef = mockDbWrongChannel
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(ADD_CHIRP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a chirp on channel which is not our own social channel fails"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(ADD_CHIRP_WRONG_CHANNEL_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Validating a RpcMessage without Params does not work in validateAddChirp"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddChirp(RPC_NO_PARAMS)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    //DeleteChirp
    test("Deleting a chirp works as intended"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteChirp(DELETE_CHIRP_RPC)
        message should equal(Left(DELETE_CHIRP_RPC))
        system.stop(dbActorRef.actorRef)
    }

    test("Deleting a chirp with invalid Timestamp fails"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteChirp(DELETE_CHIRP_WRONG_TIMESTAMP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }
    
    test("Deleting a chirp without valid PoP token fails"){
        val dbActorRef = mockDbWrongToken
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteChirp(DELETE_CHIRP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Deleting a chirp on wrong type of channel fails"){
        val dbActorRef = mockDbWrongChannel
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteChirp(DELETE_CHIRP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Deleting a chirp on channel which is not our own social channel fails"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteChirp(DELETE_CHIRP_WRONG_CHANNEL_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Validating a RpcMessage without Params does not work in validateDeleteChirp"){
        val dbActorRef = mockDbWorking
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteChirp(RPC_NO_PARAMS)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    //AddReaction
    test("Adding a reaction works as intended"){
        val dbActorRef = mockDbWorkingReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddReaction(ADD_REACTION_RPC)
        message should equal(Left(ADD_REACTION_RPC))
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a reaction with invalid Timestamp fails"){
        val dbActorRef = mockDbWorkingReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddReaction(ADD_REACTION_WRONG_TIMESTAMP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a reaction without valid PoP token fails"){
        val dbActorRef = mockDbWrongTokenReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddReaction(ADD_REACTION_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Adding a reaction on wrong type of channel fails"){
        val dbActorRef = mockDbWrongChannel
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddReaction(ADD_REACTION_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Validating a RpcMessage without Params does not work in validateAddReaction"){
        val dbActorRef = mockDbWorkingReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateAddReaction(RPC_NO_PARAMS)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    //DeleteReaction
    test("Deleting a reaction works as intended"){
        val dbActorRef = mockDbWorkingReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteReaction(DELETE_REACTION_RPC)
        message should equal(Left(DELETE_REACTION_RPC))
        system.stop(dbActorRef.actorRef)
    }

    test("Deleting a reaction with invalid Timestamp fails"){
        val dbActorRef = mockDbWorkingReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteReaction(DELETE_REACTION_WRONG_TIMESTAMP_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Deleting a reaction without valid PoP token fails"){
        val dbActorRef = mockDbWrongTokenReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteReaction(DELETE_REACTION_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Deleting a reaction on wrong type of channel fails"){
        val dbActorRef = mockDbWrongChannel
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteReaction(DELETE_REACTION_RPC)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

    test("Validating a RpcMessage without Params does not work in validateDeleteReaction"){
        val dbActorRef = mockDbWorkingReaction
        val message: GraphMessage = (new SocialMediaValidator(dbActorRef)).validateDeleteReaction(RPC_NO_PARAMS)
        message shouldBe a [Right[_,PipelineError]]
        system.stop(dbActorRef.actorRef)
    }

}
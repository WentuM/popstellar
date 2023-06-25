package ch.epfl.pop.storage

import akka.actor.{Actor, ActorLogging, Status}
import akka.event.LoggingReceive
import ch.epfl.pop.storage.SecurityModuleActor._
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm

import java.io.{DataInputStream, File, FileInputStream}
import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import scala.util.{Success, Try}

final case class SecurityModuleActor(keysFolderPath: String) extends Actor with ActorLogging {

  private val rsaPrivateKey: RSAPrivateKey = {
    val keyBytes = readBytes(keysFolderPath + "private_key.der")
    val spec = new PKCS8EncodedKeySpec(keyBytes)
    val kf = KeyFactory.getInstance("RSA")
    kf.generatePrivate(spec).asInstanceOf[RSAPrivateKey]
  }

  private val rsaPublicKey: RSAPublicKey = {
    val keyBytes = readBytes(keysFolderPath + "public_key.der")
    val spec = new X509EncodedKeySpec(keyBytes)
    val kf = KeyFactory.getInstance("RSA")
    kf.generatePublic(spec).asInstanceOf[RSAPublicKey]
  }

  private def readBytes(filePath: String): Array[Byte] = {
    val file: File = new File(filePath)
    val fis: FileInputStream = new FileInputStream(file)
    val dis: DataInputStream = new DataInputStream(fis)

    val keyBytes: Array[Byte] = new Array[Byte](file.length.asInstanceOf[Int])
    dis.readFully(keyBytes)

    keyBytes
  }

  private def signJwt(jwt: JWTCreator.Builder): String = {
    val algorithm = Algorithm.RSA256(rsaPrivateKey)
    jwt.sign(algorithm)
  }

  override def receive: Receive = LoggingReceive {
    case ReadRsaPublicKey() =>
      log.info(s"Actor $self (SecurityModuleActor) received a ReadRsaPublicKey request")
      sender() ! ReadRsaPublicKeyAck(rsaPublicKey)

    case SignJwt(jwt) =>
      log.info(s"Actor $self (SecurityModuleActor) received a SignJwt request for jwt $jwt")
      Try(signJwt(jwt)) match {
        case Success(jwtStr) => sender() ! SignJwtAck(jwtStr)
        case failure         => sender() ! failure.recover(Status.Failure(_))
      }
  }
}

object SecurityModuleActor {
  sealed trait Event

  case class ReadRsaPublicKey() extends Event

  case class SignJwt(jwt: JWTCreator.Builder) extends Event

  case class ReadRsaPublicKeyAck(publicKey: RSAPublicKey) extends Event

  case class SignJwtAck(jwt: String) extends Event
}

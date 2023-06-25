package ch.epfl.pop.storage

import akka.actor.Actor
import akka.event.LoggingReceive
import ch.epfl.pop.storage.SecurityModuleActor.{ReadRsaPublicKey, ReadRsaPublicKeyAck, SignJwt, SignJwtAck}
import com.auth0.jwt.algorithms.Algorithm

import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.{KeyPair, KeyPairGenerator}

object FakeSecurityModuleActor extends Actor {

  private val keyPair: KeyPair = {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048)
    generator.generateKeyPair
  }

  private val rsaPublicKey = keyPair.getPublic.asInstanceOf[RSAPublicKey]

  private val rsaPrivateKey = keyPair.getPrivate.asInstanceOf[RSAPrivateKey]

  override def receive: Receive = LoggingReceive {
    case ReadRsaPublicKey() => sender() ! ReadRsaPublicKeyAck(rsaPublicKey)

    case SignJwt(jwt) =>
      val algorithm = Algorithm.RSA256(rsaPrivateKey)
      sender() ! SignJwtAck(jwt.sign(algorithm))
  }
}

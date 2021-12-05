package util.examples

import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.lao.CreateLao
import ch.epfl.pop.model.network.method.message.data.rollCall.CloseRollCall
import ch.epfl.pop.model.network.method.message.data.socialMedia.AddChirp
import ch.epfl.pop.model.objects.{Base64Data, Hash, PublicKey, Signature, Timestamp, WitnessSignaturePair}
import ch.epfl.pop.json.MessageDataProtocol._
import spray.json._

object MessageExample {

  final val MESSAGE: Message = Message(
    Base64Data("eyJjcmVhdGlvbiI6MTYzMTg4NzQ5NiwiaWQiOiJ4aWdzV0ZlUG1veGxkd2txMUt1b0wzT1ZhODl4amdYalRPZEJnSldjR1drPSIsIm5hbWUiOiJoZ2dnZ2dnIiwib3JnYW5pemVyIjoidG9fa2xaTHRpSFY0NDZGdjk4T0xOZE5taS1FUDVPYVR0YkJrb3RUWUxpYz0iLCJ3aXRuZXNzZXMiOltdLCJvYmplY3QiOiJsYW8iLCJhY3Rpb24iOiJjcmVhdGUifQ=="),
    PublicKey(Base64Data("to_klZLtiHV446Fv98OLNdNmi-EP5OaTtbBkotTYLic=")),
    Signature(Base64Data("2VDJCWg11eNPUvZOnvq5YhqqIKLBcik45n-6o87aUKefmiywagivzD4o_YmjWHzYcb9qg-OgDBZbBNWSUgJICA==")),
    Hash(Base64Data("f1jTxH8TU2UGUBnikGU3wRTHjhOmIEQVmxZBK55QpsE=")),
    WitnessSignaturePair(PublicKey(Base64Data("wit1")), Signature(Base64Data("sig1"))) :: WitnessSignaturePair(PublicKey(Base64Data("wit2")), Signature(Base64Data("sig2"))) :: Nil
  )

  final val MESSAGE_FAULTY_ID: Message = Message(
    Base64Data("eyJjcmVhdGlvbiI6MTYzMTg4NzQ5NiwiaWQiOiJ4aWdzV0ZlUG1veGxkd2txMUt1b0wzT1ZhODl4amdYalRPZEJnSldjR1drPSIsIm5hbWUiOiJoZ2dnZ2dnIiwib3JnYW5pemVyIjoidG9fa2xaTHRpSFY0NDZGdjk4T0xOZE5taS1FUDVPYVR0YkJrb3RUWUxpYz0iLCJ3aXRuZXNzZXMiOltdLCJvYmplY3QiOiJsYW8iLCJhY3Rpb24iOiJjcmVhdGUifQ=="),
    PublicKey(Base64Data("to_klZLtiHV446Fv98OLNdNmi-EP5OaTtbBkotTYLic=")),
    Signature(Base64Data("2VDJCWg11eNPUvZOnvq5YhqqIKLBcik45n-6o87aUKefmiywagivzD4o_YmjWHzYcb9qg-OgDBZbBNWSUgJICA==")),
    Hash(Base64Data("FAULTY_ID")),
    WitnessSignaturePair(PublicKey(Base64Data("wit1")), Signature(Base64Data("sig1"))) :: WitnessSignaturePair(PublicKey(Base64Data("wit2")), Signature(Base64Data("sig2"))) :: Nil
  )

  //we only care about the decoded data, the rest doesn't need to be right for current testing purposes
  final val MESSAGE_CREATELAO: Message = new Message(
    Base64Data(CreateLao(Hash(Base64Data("id")), "LAO", Timestamp(0), PublicKey(Base64Data("key")), List.empty).toJson.toString),
    PublicKey(Base64Data("to_klZLtiHV446Fv98OLNdNmi-EP5OaTtbBkotTYLic=")),
    Signature(Base64Data("")),
    Hash(Base64Data("")),
    List.empty,
    Some(CreateLao(Hash(Base64Data("id")), "LAO", Timestamp(0), PublicKey(Base64Data("key")), List.empty))
  )

  final val MESSAGE_CREATELAO2: Message = new Message(
    Base64Data(CreateLao(Hash(Base64Data("id2")), "LAO2", Timestamp(0), PublicKey(Base64Data("key2")), List.empty).toJson.toString),
    PublicKey(Base64Data("to_klZLtiHV446Fv98OLNdNmi-EP5OaTtbBkotTYLic=")),
    Signature(Base64Data("")),
    Hash(Base64Data("")),
    List.empty,
    Some(CreateLao(Hash(Base64Data("id2")), "LAO2", Timestamp(0), PublicKey(Base64Data("key2")), List.empty))
  )

  final val MESSAGE_CLOSEROLLCALL: Message = new Message(
    Base64Data(CloseRollCall(Hash(Base64Data("")), Hash(Base64Data("")), Timestamp(0), List(PublicKey(Base64Data("keyAttendee")))).toJson.toString),
    PublicKey(Base64Data("to_klZLtiHV446Fv98OLNdNmi-EP5OaTtbBkotTYLic=")),
    Signature(Base64Data("")),
    Hash(Base64Data("")),
    List.empty,
    Some(CloseRollCall(Hash(Base64Data("")), Hash(Base64Data("")), Timestamp(0), List(PublicKey(Base64Data("keyAttendee")))))
  )

  final val MESSAGE_ADDCHIRP: Message = new Message(
    Base64Data(AddChirp("abc", None, Timestamp(0)).toJson.toString),
    PublicKey(Base64Data("to_klZLtiHV446Fv98OLNdNmi-EP5OaTtbBkotTYLic=")),
    Signature(Base64Data("")),
    Hash(Base64Data("")),
    List.empty,
    Some(AddChirp("abc", None, Timestamp(0)))
  )
}

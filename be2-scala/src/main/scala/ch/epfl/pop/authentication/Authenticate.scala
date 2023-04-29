package ch.epfl.pop.authentication

import akka.http.scaladsl.model.StatusCodes.ClientError
import akka.http.scaladsl.model.{AttributeKey, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import ch.epfl.pop.authentication.Authenticate.verifyResponseType

object Authenticate {
  case class RequestParameters(response_type: String, client_id: String, redirect_uri: String, scope: String, state: Option[String], response_mode: Option[String], login_hint: String, nonce: String)

  type VerificationState = Either[(String, String), Unit]

  private val mandatoryParameters = Set("response_type", "client_id", "redirect_uri", "scope", "login_hint", "nonce")

  def buildRoute(): server.Route = {
    extractRequest { request =>
      parameters(
        "response_type",
        "client_id",
        "redirect_uri",
        "scope",
        "state".optional,
        "response_mode".optional,
        "login_hint",
        "nonce"
      ) {
        (response_type, client_id, redirect_uri, scope, state, response_mode, login_hint, nonce) =>
          {
            val params = RequestParameters(response_type, client_id, redirect_uri, scope, state, response_mode, login_hint, nonce)
            verifyParameters(params) match {
              case Left(error -> errorDescription) => complete(authenticationFailure(error, errorDescription, state))
              case Right(_)                        => complete(generateChallenge(request))
            }
          }
      } ~ extractUri { uri =>
        complete {
          val attributesKeys = uri.query().toMap.keys.toSet
          val missingParams = mandatoryParameters.diff(attributesKeys).map(name => s"[$name]")
          val errorDescription = missingParams.mkString("Missing parameters: ", " ", "")
          authenticationFailure("invalid_request", errorDescription, None)
        }
      }
    }
  }

  private def verifyParameters(params: RequestParameters): VerificationState = {
    for {
      _ <- verifyResponseType(params.response_type)
      _ <- verifyScope(params.scope)
      _ <- verifyRedirectUri(params.redirect_uri)
      result <- verifyResponseMode(params.response_mode)
    } yield result
  }

  private def verifyResponseType(response_type: String): VerificationState = {
    val expectedResponseType = "id_token token"
    if (response_type == expectedResponseType) Right(())
    else
      Left("unsupported_response_type" -> s"expected \"$expectedResponseType\" but received \"$response_type\"")
  }

  private def verifyScope(scope: String): VerificationState = {
    val expectedScope = "openid"
    val scopes = scope.split(" ")
    if (scopes.contains(expectedScope)) Right(())
    else
      Left("invalid_scope" -> s"expected scope to contain \"$expectedScope\" but received \"$scope\"")
  }

  private def verifyRedirectUri(uri: String): VerificationState = {
    // Checks that the uri is a http or https format uri
    // see https://stackoverflow.com/questions/3809401/what-is-a-good-regular-expression-to-match-a-url for regex source
    val httpRegex =
      "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
    if (uri.matches(httpRegex)) Right(())
    else
      Left("invalid_request" -> s"expected http or https url format for redirect uri")
  }

  private def verifyResponseMode(mode: Option[String]): VerificationState = {
    val supportedModes = List("query", "fragment")
    if (mode.isEmpty || supportedModes.contains(mode.get)) Right(())
    else
      Left("invalid_request" -> s"only [${supportedModes.mkString(",")}] response modes are supported but received $mode")
  }

  private def generateChallenge(request: HttpRequest): HttpResponse =
    HttpResponse(status = StatusCodes.OK, entity = request.toString()) // TODO: add code for generating the challenge qrcode page

  private def authenticationFailure(error: String, errorDescription: String, state: Option[String]): HttpResponse = {
    var response = HttpResponse(status = StatusCodes.Found)
      .addAttribute(AttributeKey("error"), error)
      .addAttribute(AttributeKey("error_description"), errorDescription)
    if (state.isDefined)
      response = response.addAttribute(AttributeKey("state"), state)
    response
  }
}

package prices.services

import prices.apiClient.errors.{Exception => APIException}

import scala.util.control.NoStackTrace

object errors {

  sealed trait Exception extends NoStackTrace

  object Exception {
    case class APICallFailure(message: String) extends Exception
    case class NoFoundFailure(message: String) extends Exception
  }

  def toServiceException(apiExp: APIException): Exception =
    apiExp match {
      case APIException.APICallFailure(404, msg) => Exception.NoFoundFailure(msg)
      case APIException.APICallFailure(_, msg) => Exception.APICallFailure(msg)
    }
}

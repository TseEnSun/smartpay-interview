package prices.apiClient

import scala.util.control.NoStackTrace

object errors {

  sealed trait Exception extends NoStackTrace

  object Exception {
    final case class APICallFailure(code: Int, message: String) extends Exception
  }

}

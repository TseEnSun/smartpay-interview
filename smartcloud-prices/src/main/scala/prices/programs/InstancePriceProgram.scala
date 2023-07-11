package prices.programs

import scala.util.control.NoStackTrace

import prices.data._
import prices.services.errors.{Exception => ServiceException}

object InstancePriceProgram {

  sealed trait Exception extends NoStackTrace
  
  object Exception {
    case class ProgramFailure(message: String) extends Exception
    case class InstanceNotFound(message: String) extends Exception
  }

  def toProgramException(serviceExp: ServiceException): Exception = serviceExp match {
    case ServiceException.APICallFailure(msg) => Exception.ProgramFailure(msg)
    case ServiceException.NoFoundFailure(msg) => Exception.InstanceNotFound(msg)
  }

}

trait InstancePriceProgram[F[_]] {

  import InstancePriceProgram._
  def getPrice(instance: InstanceKind): F[Either[Exception, InstancePrice]]
  
}

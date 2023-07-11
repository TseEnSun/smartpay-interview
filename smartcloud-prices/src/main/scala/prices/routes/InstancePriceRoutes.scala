package prices.routes

import cats.implicits._
import cats.effect._
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe._
import prices.programs.InstancePriceProgram
import prices.programs.InstancePriceProgram.{Exception => ProgramException}
import prices.routes.protocol._
import prices.routes.protocol.InstancePriceResponse._
//import prices.routes.protocol.InstanceKindNotFoundResponse._


final case class InstancePriceRoutes[F[_]: Sync](instancePriceProgram: InstancePriceProgram[F]) extends Http4sDsl[F] {
  
    val prefix = "/prices"
  
    implicit val instancePriceResponseEncoder: EntityEncoder[F, InstancePriceResponse] =
      jsonEncoderOf[F, InstancePriceResponse]

    implicit val instanceKindNotFoundResponseEncoder: EntityEncoder[F, InstanceKindNotFoundResponse] =
      jsonEncoderOf[F, InstanceKindNotFoundResponse]

  
    private val get: HttpRoutes[F] = HttpRoutes.of {
      case GET -> Root :? InstanceKindQueryParam(kind) =>
        for {
          instancePrice <- instancePriceProgram.getPrice(kind)
          response <- instancePrice match {
            case Right(instancePrice) => Ok(instancePriceToResponse(instancePrice))
            case Left(ProgramException.InstanceNotFound(_)) => NotFound(InstanceKindNotFoundResponse(kind))
            case Left(ProgramException.ProgramFailure(msg)) => BadRequest(msg)
          }
        } yield response
    }
  
    def routes: HttpRoutes[F] =
      Router(
        prefix -> get
      )
}

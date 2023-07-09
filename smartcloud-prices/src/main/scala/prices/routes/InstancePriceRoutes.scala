package prices.routes

import cats.implicits._
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe._

import prices.services.InstancePriceService
import prices.routes.protocol._
import prices.routes.protocol.InstancePriceResponse._

final case class InstancePriceRoutes[F[_]: Sync](instancePriceService: InstancePriceService[F]) extends Http4sDsl[F] {
  
    val prefix = "/prices"
  
    implicit val instancePriceResponseEncoder = jsonEncoderOf[F, InstancePriceResponse]

  
    private val get: HttpRoutes[F] = HttpRoutes.of {
      case GET -> Root :? InstanceKindQueryParam(kind) =>
        for {
          instancePrice <- instancePriceService.getPrice(kind)
          response <- Ok(instancePriceToResponse(instancePrice))
        } yield response
        
    }
  
    def routes: HttpRoutes[F] =
      Router(
        prefix -> get
      )
}

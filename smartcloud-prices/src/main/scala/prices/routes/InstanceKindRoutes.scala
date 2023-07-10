package prices.routes

import cats.implicits._
import cats.effect._
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import prices.routes.protocol._
import prices.services.InstanceKindService

final case class InstanceKindRoutes[F[_]: Sync](instanceKindService: InstanceKindService[F]) extends Http4sDsl[F] {

  val prefix = "/instance-kinds"

  implicit val instanceKindResponseEncoder: EntityEncoder[F, List[InstanceKindResponse]] =
    jsonEncoderOf[F, List[InstanceKindResponse]]

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root =>
      instanceKindService.getAll.flatMap {
        case Right(ks) => Ok(ks.map(k => InstanceKindResponse(k)))
        case Left(exp) => InternalServerError(exp.getMessage)
      }
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}

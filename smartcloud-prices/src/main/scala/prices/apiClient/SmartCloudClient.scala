package prices.apiClient

import scala.concurrent.duration._

import cats.syntax.all._
import cats.effect._
import org.http4s._
import org.http4s.Method._
import org.http4s.client._
import org.http4s.client.middleware._
import org.http4s.headers._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._

import prices.data._
import prices.config.Config._
import prices.apiClient.errors.Exception
import prices.apiClient.errors.Exception._
import SmartCloudProtocol._


trait SmartCloudClient[F[_]] {

  def getInstanceKinds: F[Either[Exception, List[String]]]
  def getPrice(instance: InstanceKind): F[Either[Exception, SmartCloudPrice]]

}

object SmartCloudClient {

  def make[F[_]: Temporal](
    config: SmartCloudConfig,
    client: Client[F]
  ): SmartCloudClient[F] = new SmartCloudClient[F] with Http4sClientDsl[F] {

    implicit val decoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    val retryPolicy: RetryPolicy[F] = RetryPolicy[F] { (attempts: Int) =>
      if (attempts >=config.maxRetry) None
      else Some(10.milliseconds)
    }
    val retryClient: Client[F] = Retry[F](retryPolicy)(client)

    def getInstanceKinds: F[Either[Exception, List[String]]] =
      Uri.fromString(s"${config.baseUri}/instances").liftTo[F].flatMap { uri =>
        val request = GET(uri, Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
        retryClient.run(request).use { response =>
          response.status match {
            case Status.Ok => response.as[List[String]].map(_.asRight)
            case st => Either.left[Exception, List[String]](APICallFailure(st.code, st.reason)).pure[F]
          }
        }
      }

    def getPrice(instance: InstanceKind): F[Either[Exception, SmartCloudPrice]] =
      Uri.fromString(s"${config.baseUri}/instances/${instance.getString}").liftTo[F].flatMap { uri =>
        val request = GET(uri, Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
        retryClient.run(request).use { response =>
          response.status match {
            case Status.Ok => response.asJsonDecode[SmartCloudPrice].map(_.asRight)
            case st => Either.left[Exception, SmartCloudPrice](APICallFailure(st.code, st.reason)).pure[F]
          }
        }
      }
  }
  
}

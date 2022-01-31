import cats.syntax.all._
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global

object API {

  trait DirectoryRepo[F[_]] {
    def find(fullPath: String): F[Option[Directory]]
  }

  implicit val DirectoryEncoder
    : Encoder[Directory] = deriveEncoder[Directory] // todo: move it somewhere else xD

  val healthCheckService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "healthCheck" =>
      IO(Response(status = Status.Ok))
  }

  def apiServices[F[_]](repo: DirectoryRepo[F])(
      implicit F: Async[F]
  ): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "stats" / path =>
      repo.find(path).map {
        case Some(user) => Response(status = Status.Ok).withEntity(user.asJson)
        case None       => Response(status = Status.NotFound)
      }
  }
}

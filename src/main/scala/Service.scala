import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

object Service extends Http4sDsl[IO] with LazyLogging {

  implicit val simpleDirectoryDecoder: Decoder[SimpleDirectory] =
    deriveDecoder[SimpleDirectory]
  implicit val directoryEncoder: Encoder[Directory] = deriveEncoder[Directory]

  object URLVar {
    def unapply(str: String): Option[String] = {
      if (str.nonEmpty)
        Some(str)
      else
        None
    }
  }

  def routes(ref: Ref[IO, Seq[Directory]]): HttpRoutes[IO] = {
    val directories: IO[Seq[Directory]] = ref.get

    HttpRoutes
      .of[IO] {
        case req @ POST -> Root / "api" / "shorten" =>
          for {
            data <- req.as[SimpleDirectory]
            directory <- IO.delay(Directory(data.uri, data.generateShort))
            _ <- ref.getAndUpdate(_ :+ directory)
            response <- Ok(directory)
          } yield response

        case GET -> Root / "api" / "stats" => Ok(directories)

        // to improve
        case GET -> Root / "api" / URLVar(url) =>
          directories.map(_.exists(_.shortURL.get == url)).flatMap {
            case false =>
              // fullURL
              directories.map(_.exists(_.fullURL == url)).flatMap {
                case false => BadRequest()
                case _ =>
                  for {
                    maybeDir <- directories
                      .map(_.filter(_.fullURL == url))
                      .option
                    dir = maybeDir.get.head.next
                    response <- Ok(dir)
                  } yield response
              }
            case _ =>
              // shortURL
              for {
                maybeDir <- directories
                  .map(_.filter(_.shortURL.get == url))
                  .option
                dir = maybeDir.get.head.next
                _ <- ref.tryUpdate(_.filterNot(_.fullURL == dir.fullURL) :+ dir)
                response <- Ok(dir)
              } yield response
          }

        case GET -> Root / "api" / "stats" / URLVar(fullURL) =>
          directories.map(_.filter(_.fullURL == fullURL)).map {
            case Seq(dir) =>
              val counts = Counts(dir.hits)
              Response(Status.Ok).withEntity(counts)
            case _ => Response(Status.BadRequest)
          }
      }
  }

}

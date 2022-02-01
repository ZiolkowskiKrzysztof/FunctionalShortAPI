import cats.effect._
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.http4sLiteralsSyntax

case class Service extends Http4sDsl[IO] {

  implicit val simpleDirectoryDecoder: Decoder[SimpleDirectory] =
    deriveDecoder[SimpleDirectory]
  implicit val directoryEncoder: Encoder[Directory] = deriveEncoder[Directory]

//  def updateDirectories(directory: Directory) = {
//    val request = Request(method = Method.GET, uri = uri"/api/ok")
//    Service(directory :: directoryRepo).serviceRoutes.run(request)
//  }

  def serviceRoutes(
      directoryRepo: List[Directory] = List.empty): HttpRoutes[IO] =
    HttpRoutes
      .of[IO] {
        case req @ POST -> Root / "api" / "shorten" =>
          for {
            data <- req.as[SimpleDirectory]
//            _ <- serviceRoutes(
//              Directory(data.uri, data.generateShort) :: directoryRepo)
            response <- Ok(Directory(data.uri, data.generateShort))
          } yield response

        case GET -> Root / "api" / "ok" =>
          Ok()
      }

}

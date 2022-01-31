import API.DirectoryRepo
import org.scalatest.flatspec.AnyFlatSpec
import cats.effect._
import cats.effect.unsafe.IORuntime
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.jsonDecoder
import io.circe.syntax._
import io.circe.Json

class ApiTest extends AnyFlatSpec {
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  def check[A](actual: IO[Response[IO]],
               expectedStatus: Status,
               expectedBody: Option[A])(
      implicit ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp = actual.unsafeRunSync()
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      // Verify Response's body is empty.
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty)(
      expected => actualResp.as[A].unsafeRunSync() == expected
    )
    statusCheck && bodyCheck
  }

  "GET /healthCheck" should "return Ok" in {
    val response = API.healthCheckService.orNotFound
      .run(Request(method = Method.GET, uri = uri"/healthCheck"))

    check[Json](response, Status.Ok, None)
  }

  "POST /api/shorten" should "create a new short" in {
    ???
  }

  "GET /api/stats" should "return the full list of shorten urls" in {
    ???
//    val fullUrl = "google.com"
//    val shortUrl = Option("g.com") // todo: to .option
//    val hits = 1
//
//    val success: DirectoryRepo[IO] = new DirectoryRepo[IO] {
//      def find(id: String): IO[Option[Directory]] =
//        IO.pure(Some(Directory(fullUrl, shortUrl, 1)))
//    }
//
//    val response: IO[Response[IO]] = API
//      .service[IO](success)
//      .orNotFound
//      .run(
//        Request(method = Method.GET, uri = uri"/stats/google.com")
//      )
//
//    val expectedJson = Json.obj(
//      "fullURL" := fullUrl,
//      "shortURL" := shortUrl,
//      "hits" := hits
//    )
//
//    assert(check[Json](response, Status.Ok, Some(expectedJson)))
  }

  "GET /api/stats/<short>" should "response with number of hits" in {
    ???
  }

  "GET /api/<short>" should "redirects to original URI" in {
    ???
  }

}

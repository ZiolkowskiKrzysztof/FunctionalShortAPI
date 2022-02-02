import cats.effect._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  val cleanRef: Ref[IO, Seq[Directory]] =
    Ref.unsafe(Seq.empty)

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(Service.routes(cleanRef).orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}

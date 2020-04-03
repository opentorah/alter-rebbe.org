package org.opentorah.collector

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.{Charset, HttpRoutes, /*QueryParamDecoder,*/ Response, StaticFile}
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import org.http4s.server.blaze.BlazeServerBuilder
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object Service extends IOApp {

  private val blockingPool: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  private val blocker: Blocker = Blocker.liftExecutorService(blockingPool)

  private val staticResourceExtensions: Seq[String] = Seq(".ico", ".css", ".js", ".jpeg")

  private val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root / path if staticResourceExtensions.exists(path.endsWith) =>
      StaticFile.fromResource("/" + path, blocker, Some(request))
        .getOrElseF(NotFound())
  }

  def renderHtml(content: String): IO[Response[IO]] = Ok(content).map(
    _.withContentType(`Content-Type`(MediaType.`text`.`html`, Charset.`UTF-8`))
  )

  // To be accessible when running in a docker container the server must bind to all IPs, not just 127.0.0.1:
  override def run(args: List[String]): IO[ExitCode] = BlazeServerBuilder[IO]
    .bindHttp(host = "0.0.0.0", port = getServicePort)
    .withHttpApp(service.orNotFound)
    .serve
    .compile
    .drain
    .as(ExitCode.Success)

  private def getServicePort: Int =
    scala.util.Properties.envOrNone("SERVICE_PORT").map(_.toInt).getOrElse(8090)
}

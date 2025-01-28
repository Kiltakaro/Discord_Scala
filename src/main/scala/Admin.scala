import cats.effect._

import com.comcast.ip4s._


// import io.circe.generic.auto._
// import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.ember.server.EmberServerBuilder


// de ce que j'ai compris
// On peut exetends IOApp.Simple plutot que IOApp mais faudra remplacer run par 
//  def run: IO[Unit] = EmberServerBuilder
// et on aura plus besoin du
// ".as(ExitCode.Success)"
// mais je comprends pas pourquoi le serv s'arrete avec ça :/ 
object Admin extends IOApp {

    // Définir des routes
    // https://http4s.org/v1/docs/middleware.html#composing-services-with-middleware
    val adminRoutes = HttpRoutes.of[IO] {
        case GET -> Root / "admin" / "ping" =>
            Ok("Admin is up!")
        case _ =>
            NotFound("Admin Error")
    }

    // https://http4s.org/v1/docs/json.html#a-hello-world-service
    // app avec nos routes 
    val httpApp = adminRoutes.orNotFound

    // https://http4s.org/v1/docs/client.html#setup
    def run(args: List[String]): IO[ExitCode] = EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp) // donner les routes au serveur
        .build
        .use(_ => IO.never) // ne pas fermer le serveur
        .as(ExitCode.Success)
}

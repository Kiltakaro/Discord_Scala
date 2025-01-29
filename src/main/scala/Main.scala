import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory


// On peut exetends IOApp plutot que IOApp.Simple mais ça a l'air plus simple la version "IOApp.Simple"
// a voir plus tard
object Main extends IOApp.Simple {

    given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger = loggerFactory.getLogger
    // val finalHttpApp = Logger.httpApp(true, true)(app)

    // https://http4s.org/v1/docs/client.html#setup
    // lancement de "serveur"
    val run: IO[Unit] = for {
        _ <- logger.info("Starting Admin Server...")
        _ <- EmberServerBuilder.default[IO]
            .withHost(ipv4"0.0.0.0")
            .withPort(port"8080")
            .withHttpApp(Admin.httpApp)
            .build
            .use(_ => IO.never)
    } yield ()
}

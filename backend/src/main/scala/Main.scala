import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.typelevel.log4cats.LoggerFactory
import org.http4s.server.middleware.{CORS, CORSConfig, Logger}
import org.http4s.Method

import org.typelevel.log4cats.slf4j.Slf4jFactory

import org.http4s.server.Router

import cats.data.NonEmptyList
import cats.implicits._
import java.util.UUID
import org.http4s.server.Server
import org.http4s.HttpApp


// On peut exetends IOApp plutot que IOApp.Simple mais ça a l'air plus simple la version "IOApp.Simple"
// a voir plus tard
object Main extends IOApp {

    given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger = loggerFactory.getLogger

    // https://stackoverflow.com/questions/58446033/how-to-combine-authedroutes-and-httproutes-in-http4s

    // https://http4s.org/v1/docs/service.html

    

    // Démarrage du serveur
    def startServer(finalHttpApp: HttpApp[IO]): IO[ExitCode] = {
        EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }

    /////////////////// LANCEMENT /////////////////////////

    // https://http4s.org/v1/docs/client.html#setup
    // Lancement du serveur et de la connexion à la BDD simultanément
    def run(args: List[String]): IO[ExitCode] = {

        // je cherche plein de trucs pour les erreurs CORS mais je trouve pas la solution
        // par contre on arrive a faire des quetes sans etre bloqué, enfin je crois
        Database.clickhouseTransactor.use { xa =>
            val corsConfig = CORSConfig.default
                .withAnyOrigin(true)
                .withAllowedMethods(Some(Set(Method.GET, Method.POST, Method.PUT, Method.DELETE)))
                .withAllowedHeaders(Some(Set("Content-Type", "Authorization")))
                // .withAllowedHeaders(Some(Set("Accept", "Content-Type", "Origin", "X-Json", "X-Prototype-Version", "X-Requested-With")))
                // .withAllowCredentials(true)

            val finalHttpApp = Logger.httpApp(true, true)(
                CORS(
                    Router(
                        "/admin" -> Admin.adminRoutes,
                        "/users" -> User.userRoutes(xa),
                        "/guilds" -> Guild.guildRoutes(xa),
                        "/channels" -> Channel.channelRoutes(xa),
                        "/auth" -> Authentification.authentificationRoutes(xa),
                        "/friends" -> Friend.friendRoutes(xa)
                    ).orNotFound,
                    corsConfig
                )
            )
            startServer(finalHttpApp)
        }
    }
}

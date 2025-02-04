import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import org.http4s.Request
import org.http4s.Response
import cats.effect._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import org.http4s.server.Server

// On utilise ce fichier là pour lancer le serveur
object HttpServer {

    // https://stackoverflow.com/questions/58446033/how-to-combine-authedroutes-and-httproutes-in-http4s

    // OK Router fonctionne 
    // https://http4s.org/v1/docs/service.html
    
    val finalHttpApp = Logger.httpApp(true, true)(
        Router(
            "/admin" -> Admin.adminRoutes,
            "/users" -> User.userRoutes
        ).orNotFound
    )


    // Démarrage du serveur
    def startServer[F[_]: Async]: Resource[IO, Server] = {
        EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    }
}

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

    // Combiner les routes ! 
    // faut trouver un moyen de faire un truc comme ça 
    // val finalHttpApp = Logger.httpApp(true, true)(app)
    // ====>
    // app = Admin.httpApp + User.httpApp
    // val finalHttpApp = Logger.httpApp(true, true) (app)
    // 
    // ce truc devrait marcher, ça run mais on peut juste pas acceder aux routes du 2eme
    
    // https://stackoverflow.com/questions/58446033/how-to-combine-authedroutes-and-httproutes-in-http4s
    // val app = Admin.httpApp <+> User.httpApp
    // val finalHttpApp = Logger.httpApp(true, true)(Admin.httpApp <+> User.httpApp)

    // Exactement le meme probleme avec ça 
    // val app = User.httpApp.combineK(Admin.httpApp)
    // val finalHttpApp = Logger.httpApp(true, true)(app)

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

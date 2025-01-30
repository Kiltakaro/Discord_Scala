import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.typelevel.log4cats.LoggerFactory
import org.http4s.server.middleware.Logger

// import cats.syntax.semigroupk._
// import cats.SemigroupK.nonInheritedOps.toSemigroupKOps
// import cats.SemigroupK.ops.toAllSemigroupKOps
// import cats.implicits.toSemigroupKOps
// import cats.syntax.all.toSemigroupKOps
// import cats.syntax.semigroupk.toSemigroupKOps

import org.typelevel.log4cats.slf4j.Slf4jFactory

import org.http4s.server.Router


// On peut exetends IOApp plutot que IOApp.Simple mais ça a l'air plus simple la version "IOApp.Simple"
// a voir plus tard
object Main extends IOApp.Simple {

    given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger = loggerFactory.getLogger

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



    /////////////////// LANCEMENT /////////////////////////

    // https://http4s.org/v1/docs/client.html#setup
    // lancement de "serveur"
    val run: IO[Unit] = for {
        _ <- logger.info("Starting Admin Server...")
        _ <- EmberServerBuilder.default[IO]
            .withHost(ipv4"0.0.0.0")
            .withPort(port"8080")
            .withHttpApp(finalHttpApp)
            .build
            .use(_ => IO.never)
    } yield ()
}

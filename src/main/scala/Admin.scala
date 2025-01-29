import cats.effect._

import com.comcast.ip4s._


import io.circe.generic.auto._
import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.ember.server.EmberServerBuilder

// model reponse pour json
case class AdminStatus(status: String)
case class User(id: Int, name: String, isAdmin: Boolean)


// Je comprends pas trop comment designer 
// pour le moment je l'ai nommé admin mais c'est un peu le bordel en fait
// je vois pas comment gerer l'admin psk le USER peut etre plusieur fois admin ou non etc aled
object Admin {

    val users = List(
        User(1, "Tanny", true),
        User(2, "Secours", false)
    )

    // Définir des routes
    // https://http4s.org/v1/docs/middleware.html#composing-services-with-middleware
    val adminRoutes = HttpRoutes.of[IO] {
        case GET -> Root / "admin" / "ping" =>
            Ok(AdminStatus("Admin route works!").asJson)

        case GET -> Root / "admin" / "users" =>
            Ok(users.asJson)

        // TOUJOURS LAISSER A LA FIN
        case _ =>
            NotFound("Admin Error")
    }

    // https://http4s.org/v1/docs/json.html#a-hello-world-service
    // app avec nos routes 
    val httpApp = adminRoutes.orNotFound

}

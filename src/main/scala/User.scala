import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

case class User(id: Int, name: String, isAdmin: Boolean)

object User {

    // stockage temporaire users
    // faudra ajouter clickhouse
    val users = List(
        User(1, "Tanny", true),
        User(2, "Secours", false),
    )


    // PLUS BESOIN DE METTRE USER DANS LA ROUTE CAR IL EST DANS LE ROUTEUR
    val userRoutes= HttpRoutes.of[IO] {
        case GET -> Root =>
            Ok(users.asJson) // Renvoie la liste en JSON

         // Pour mettre un String dans une route
        case GET -> Root / "hello" / name =>
            Ok(s"Hello, $name.")

        
        // le IntVar() pour mettre des int dans les routes
        case GET -> Root / IntVar(id) =>
            users.find(_.id == id) match {
                case Some(user) => 
                    Ok(user.asJson)
                case None => 
                    NotFound(s"No user with id : $id")
            }
        
        // TOUJOURS LAISSER A LA FIN
        case GET -> Root / _ =>
            NotFound("User Route Not FOund")
    }

    // https://http4s.org/v1/docs/json.html#a-hello-world-service
    // app avec nos routes 
    val httpApp: HttpApp[IO] = userRoutes.orNotFound
}

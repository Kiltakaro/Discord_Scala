import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityDecoder._

import cats.effect.IO
// import cats.effect.concurrent.Ref
import cats.implicits._


// Oui on utilisera des UUID plus tard
case class User(id: Int, name: String, isAdmin: Boolean)

case class UserInput(name: String)

object User {

    // on peut pas faire += comme tout le monde...
    // https://www.oreilly.com/library/view/scala-cookbook/9781449340292/ch11s04.html
    def addUser(user: UserInput): IO[Response[IO]] = {
        users = User(3, user.name, false) :: users
        Ok(user.asJson)
    }

    def deleteUser(id: Int): IO[Response[IO]] = {
        users.find(_.id == id) match {
        case Some(user) =>
            // ajouter un delete sur la bdd
            Ok("deleted")
        case None =>
            NotFound(s"No user with id $id found")
        }
    }

    // stockage temporaire users
    // JB a dit pas de VAR donc faudra surement changer pour des Ref plus tard
    // mais tfacon les users seront dans une BDD
    var users = List(
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

        
        // Pour tester : 
        // curl -X POST http://localhost:8080/users/echo -d test
        // https://http4s.org/v1/docs/server-middleware.html
        case r @ POST -> Root / "echo" => 
            r.as[String].flatMap(Ok(_))


        ///////////////////////// CRUD /////////////////////////////

        // faudra peut etre enlever le mot "Create" dans la route
        // j'improve ça la prochaine fois 
        // change add => create pour CRUD
        case r @ POST -> Root / "create" =>
            r.as[UserInput].attempt.flatMap {
                case Right(user: UserInput) =>
                    if (user.name.length > 0) {
                        addUser(user)
                        Ok(user.asJson)
                    }
                    else {
                        BadRequest("Name must be longer")
                    }
                case Left(_) =>
                    BadRequest("Error format {name: String}")
        }


        // READ
        // le IntVar() pour mettre des int dans les routes
        case GET -> Root / IntVar(id) =>
            users.find(_.id == id) match {
                case Some(user) => 
                    Ok(user.asJson)
                case None => 
                    NotFound(s"No user with id : $id")
            }

        // case DELETE -> Root / IntVar(id) =>
        //     deleteUser(id)
        //     BadRequest("Error no")

=        // TOUJOURS LAISSER A LA FIN
        case GET -> Root / _ =>
            NotFound("User Route Not FOund")
    }

    // https://http4s.org/v1/docs/json.html#a-hello-world-service
    // app avec nos routes 
    val httpApp: HttpApp[IO] = userRoutes.orNotFound
}

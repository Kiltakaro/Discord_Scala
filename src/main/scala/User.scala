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
import doobie.util.transactor.Transactor
import doobie.implicits._
import javax.xml.crypto.Data
import java.util.UUID
import doobie.util.meta.Meta


// Oui on utilisera des UUID plus tard
case class User(id: UUID, name: String, password: String, isAdmin: Boolean)

case class UserInput(name: String, password: String, isAdmin: Boolean)

object User {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    // on peut pas faire += comme tout le monde...
    // https://www.oreilly.com/library/view/scala-cookbook/9781449340292/ch11s04.html
    def addUser(user: UserInput, xa: Transactor[IO]): IO[Int] = {
        // users = User(3, user.name, false) :: users
        // Ok(user.asJson)

        // val userId = UUID.randomUUID().toString // toString() psk sinon ça bug voila pensez y.
        // val userId2 = "88888888-8888-8888-8888-888888888888" // Fake UUID et Avec celui ci vous voyez VRAIMENT que ça marche

        val insertUser = // flemme de typer
        sql"""
            INSERT INTO User (username, password)
            VALUES (${user.name}, ${user.password})
        """.update.run
        insertUser.transact(xa)
    }

    // Récupère tous les user dans la bdd (faudra adapter pour récupérer seulement ceux d'un certain serveur)
    def getAllUsers(xa: Transactor[IO]): IO[List[(UUID, String, String)]] = {
        val query = sql"SELECT user_id, username, password FROM User".query[(UUID, String, String)]
        val queryToList: doobie.ConnectionIO[List[(UUID, String, String)]] = query.to[List]
        queryToList.transact(xa)
    }

    def getUserById(id: UUID, xa: Transactor[IO]): IO[Option[(UUID, String, String)]]= {
        sql"SELECT user_id, username, password FROM User WHERE user_id = $id"
        .query[(UUID, String, String)]
        .option
        .transact(xa)
    }

    def deleteUser(id: UUID, xa: Transactor[IO]): IO[Int] = {
        val deleteUser = sql"DELETE FROM User WHERE user_id = $id"
        .update
        .run
        deleteUser.transact(xa)
     }

     def updateUser(id: UUID, user: UserInput, xa: Transactor[IO]): IO[Int] = {
        sql"ALTER TABLE User UPDATE username=${user.name}, password=${user.password} WHERE user_id = $id"
        .update
        .run
        .transact(xa)
     }


    // stockage temporaire users
    // JB a dit pas de VAR donc faudra surement changer pour des Ref plus tard
    // mais tfacon les users seront dans une BDDOption[UUID, String, String]

    // Maintenant on a plus besoin de ça, je laisse quand même au cas où pour des tests
    /*
    var users = List(
        User(1, "Tanny", "abc", true),
        User(2, "Secours", "def", false),
    )*/


    // PLUS BESOIN DE METTRE USER DANS LA ROUTE CAR IL EST DANS LE ROUTEUR
    def userRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {
            // READ
            // Attention la requête c'est /users/<uuid> et pas /users?id=<uuid>, ça peut porter à confusion l'id n'est pas un paramètre
            case GET -> Root / UUIDVar(id) =>
                getUserById(id, xa).flatMap { 
                    userOption => 
                        userOption match {
                        case Some((id, username, password)) => 
                            Ok((id, username, password).asJson)

                        case None =>
                            NotFound(s"No user with ID : $id")

                        }
                }
            
            // Recup tous les user
            case GET -> Root =>
                getAllUsers(xa).flatMap { users => 
                    Ok(users.asJson)
                }

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
                            addUser(user, xa).flatMap { result =>
                                Ok(s"rows affected : $result")
                            }
                        }
                        else {
                            BadRequest("Name must be longer")
                        }
                    case Left(_) =>
                        BadRequest("Error format {name: String, password: String, isAdmin: Boolean}")
                }

            // Mettre à jour un user. C'est en gros le même principe que pour l'ajout à part qu'on check si le user existe avant
            case r @ PUT -> Root / UUIDVar(id) => 
                getUserById(id, xa).flatMap { userOption =>
                    userOption match { 
                        case Some(user) => 
                            r.as[UserInput].attempt.flatMap {
                                case Right(user) => 
                                    if(user.name.length > 0) {
                                        updateUser(id, user, xa).flatMap { result =>
                                            Ok(s"Rows affected : $result")
                                        }
                                    } else {
                                        BadRequest("Username must not be empty")
                                    }
                                
                                case Left(_) =>
                                    BadRequest("Bad request. Format : {name: String, password: String, isAdmin: Boolean}")
                            }

                        case None => 
                            NotFound(s"Could not update user with id $id : not found")
                    }
                }
            

            // Supprime un user
            case DELETE -> Root / UUIDVar(id) =>
                getUserById(id, xa).flatMap {
                    userOption =>
                        userOption match {

                            case Some(user) => 
                                deleteUser(id, xa).flatMap {
                                    result =>
                                        Ok(s"Affected rows : $result")
                                }

                            case None => 
                                NotFound(s"Couldn't delete user with id $id : not found")
                        }

                }

            
            // Route not found à laisser
            case GET -> Root / _ =>
                NotFound("User Route Not Found")
            }
        }
                        // https://http4s.org/v1/docs/json.html#a-hello-world-service
                        // app avec nos routes 
                        // val httpApp: HttpApp[IO] = userRoutes.orNotFound
}

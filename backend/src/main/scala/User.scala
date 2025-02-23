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
import io.circe.Json


import at.favre.lib.crypto.bcrypt.BCrypt

// import com.github.t3hnar.bcrypt._
// import scala.util.{Success, Failure}

case class UserOutput(uuid: UUID, username: String)

case class UserInput(username: String, password: String, email: String)

case class ChangePassword(newPassword: String, oldPassword: String)

object User {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    // on peut pas faire += comme tout le monde...
    // https://www.oreilly.com/library/view/scala-cookbook/9781449340292/ch11s04.html
    // renommer en create plus tard (add on dirait que c'est pour rajouter dans la guild)
    def addUser(user: UserInput, xa: Transactor[IO]): IO[Int] = {
    
        val hashedPassword = BCrypt.withDefaults().hashToString(12, user.password.toCharArray)
        val insertUser =
        sql"""
            INSERT INTO User (username, password, email)
            VALUES (${user.username}, $hashedPassword, ${user.email})
        """.update.run
        insertUser.transact(xa)
    }

    // Récupère tous les user dans la bdd (faudra adapter pour récupérer seulement ceux d'un certain serveur)
    def getAllUsers(xa: Transactor[IO]): IO[List[(UUID, String, String, String)]] = {
        val query = sql"SELECT user_id, username, email, password FROM User".query[(UUID, String, String, String)]
        val queryToList: doobie.ConnectionIO[List[(UUID, String, String, String)]] = query.to[List]
        queryToList.transact(xa)
    }

    // Ici je fais en sorte que la bdd renvoie tous les users qui ont leur username qui commence par <username>
    // Ce sera plus pratique si on veut faire une barre de recherche
    def getUsersByUsername(username: String, xa: Transactor[IO]): IO[List[UserOutput]] = {
        sql"""
            SELECT user_id, username FROM User
            WHERE startsWith(username, $username)
        """.query[UserOutput].to[List].transact(xa)
        // On va eviter de renvoyer le mot de passe aux utilisateurs ^^
    }

    // Duplicat de getGuilds2 pour ne pas péter le système d'invitation
    def getGuilds(user_id: UUID, xa: Transactor[IO]): IO[List[(GuildInviteOutput)]]  = {
        sql"""
            SELECT guild_id, guild_name FROM 
            User_Guild JOIN Guild ON User_Guild.guild_id = Guild.guild_id
            WHERE user_id = ${user_id.toString}
        """
        .query[GuildInviteOutput]
        .to[List]
        .transact(xa)
    }

    // check ligne 178 pour utiliser celle ci en attendant
    def getGuilds2(user_id: UUID, xa: Transactor[IO]): IO[List[(GuildInviteOutput)]]  = {
        sql"""
            SELECT guild_id, guild_name FROM 
            User_Guild JOIN Guild ON User_Guild.guild_id = Guild.guild_id
            WHERE user_id = ${user_id.toString} AND invite_accepted = 1
        """
        .query[GuildInviteOutput]
        .to[List]
        .transact(xa)
    }

    def getUserById(id: UUID, xa: Transactor[IO]): IO[Option[(UUID, String, String, String)]]= {
        sql"SELECT user_id, username, password, email FROM User WHERE user_id = $id"
        .query[(UUID, String, String, String)]
        .option
        .transact(xa)
    }

    def deleteUser(id: UUID, xa: Transactor[IO]): IO[Int] = {
        val deleteUser = sql"DELETE FROM User WHERE user_id = $id"
        .update
        .run
        deleteUser.transact(xa)
    }

    // un peu redondant avec la fonction de login mais bon
    // en gros si une action demande de verifier le mdp, on a ça qui fait le boulot
    def checkPasswords(id: UUID, oldPassword: String, xa: Transactor[IO]): IO[Boolean] = {
        sql"SELECT password FROM User WHERE user_id = $id"
        .query[String]
        .option
        .transact(xa)
        .map {
            case Some(hashedPassword) =>
                val passwordToCharArray = oldPassword.toCharArray
                BCrypt.verifyer().verify(passwordToCharArray, hashedPassword).verified
            case None => false
        }
    }

    /////////////////////////// UPDATES /////////////////////////////

    def updateUser(id: UUID, user: UserInput, xa: Transactor[IO]): IO[Int] = {

        val hashedPassword = BCrypt.withDefaults().hashToString(12, user.password.toCharArray)
        sql"ALTER TABLE User UPDATE username=${user.username}, password=$hashedPassword, email=${user.email} WHERE user_id = $id"
        .update
        .run
        .transact(xa)
    }

    def changePassword(id: UUID, newPassword: String, xa: Transactor[IO]): IO[Int] = {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray)
        sql"ALTER TABLE User UPDATE password=$hashedPassword WHERE user_id = $id"
        .update
        .run
        .transact(xa)
    }

    def changeUsername(id: UUID, newUsername: String, xa: Transactor[IO]): IO[Int] = {
        sql"ALTER TABLE User UPDATE username=$newUsername WHERE user_id = $id"
        .update
        .run
        .transact(xa)
    }

    def changeEmail(id: UUID, newEmail: String, xa: Transactor[IO]): IO[Int] = {
        sql"ALTER TABLE User UPDATE email=$newEmail WHERE user_id = $id"
        .update
        .run
        .transact(xa)
    }


    def userRoutes(xa: Transactor[IO]): HttpRoutes[IO] = {
        HttpRoutes.of[IO] {
            ///////////////////////// CRUD /////////////////////////////

            // READ
            // Attention la requête c'est /users/<uuid> et pas /users?id=<uuid>, ça peut porter à confusion l'id n'est pas un paramètre
            case GET -> Root / UUIDVar(id) =>
                getUserById(id, xa).flatMap { 
                    userOption => 
                        userOption match {
                        case Some((id, username, password, email)) => 
                            Ok((id, username, password, email).asJson)

                        case None =>
                            NotFound(s"No user with ID : $id")
                        }
                }

            // Recup tous les users en fonction de leur username
            case GET -> Root / username =>
                getUsersByUsername(username, xa).flatMap { users =>
                    Ok(users.asJson)
                }

            // Recup la liste des guilds d'un user
            case GET -> Root / UUIDVar(id) / "guilds" =>
                getGuilds2(id, xa).flatMap { guilds =>
                    Ok(guilds.asJson)
            } 
            
            // Recup tous les user
            case GET -> Root =>
                getAllUsers(xa).flatMap { users => 
                    Ok(users.asJson)
                }


            // Route pour create User
            case r @ POST -> Root / "create" =>
                r.as[UserInput].attempt.flatMap {
                    case Right(user: UserInput) =>
                        // rajouter un test pour si email non nul ?
                        if (user.username.nonEmpty) {
                            addUser(user, xa).flatMap { result =>
                                Ok(s"rows affected : $result")
                            }
                        }
                        else {
                            BadRequest("username must be longer")
                        }
                    case Left(_) =>
                        BadRequest("Error format {username: String, password: String, email: String}")
                }

            //////////////// CHANGE TOUT 
            // Mettre à jour un user. C'est en gros le même principe que pour l'ajout à part qu'on check si le user existe avant
            case r @ PUT -> Root / UUIDVar(id) => 
                getUserById(id, xa).flatMap { userOption =>
                    userOption match { 
                        case Some(user) => 
                            r.as[UserInput].attempt.flatMap {
                                case Right(user) => 
                                    // ajouter le meme test pour email ?
                                    if(user.username.nonEmpty) {
                                        updateUser(id, user, xa).flatMap { result =>
                                            Ok(s"Rows affected : $result")
                                        }
                                    } else {
                                        BadRequest("Username must not be empty")
                                    }
                                
                                case Left(_) =>
                                    BadRequest("Bad request. Format : {username: String, password: String, email: String}")
                            }

                        case None => 
                            NotFound(s"Could not update user with id $id : not found")
                    }
                }


            // Ne modifie que le password
            case r @ PUT -> Root / UUIDVar(id) / "password" => 
                getUserById(id, xa).flatMap { passwordsOption =>
                    passwordsOption match { 
                        case Some(passwords) => 
                            r.as[ChangePassword].attempt.flatMap {
                                case Right(passwords) => 
                                    checkPasswords(id, passwords.oldPassword, xa).flatMap { isValid =>
                                        if(isValid) {
                                            if(passwords.newPassword.nonEmpty) {
                                                changePassword(id, passwords.newPassword, xa).flatMap { result =>
                                                    Ok(s"Rows affected : $result")
                                                }
                                            } else {
                                                BadRequest("NewPassword must not be empty")
                                            }
                                        }
                                        else {
                                            BadRequest("Old password is incorrect")
                                        }
                                    }
                                case Left(_) =>
                                    BadRequest("Bad request. Format : {oldPassword: String, newPassword: String}")
                            }

                        case None => 
                            NotFound(s"Could not update user with id $id : not found")
                    }
                }

            // Ne modifie que l'email
            case r @ PUT -> Root / UUIDVar(id) / "email" => 
                getUserById(id, xa).flatMap { emailOption =>
                    emailOption match { 
                        case Some(email) => 
                            r.as[Json].flatMap { json =>
                                val newEmail = json.hcursor.get[String]("email").getOrElse("")
                                if(newEmail.nonEmpty) {
                                    changeEmail(id, newEmail, xa).flatMap { result =>
                                        Ok(s"Rows affected : $result")
                                    }
                                } else {
                                    BadRequest("Email must not be empty")
                                }
                            }
                        case None => 
                            NotFound(s"Could not update user with id $id : not found")
                    }
                }
            
            // Ne modifie que le username
            case r @ PUT -> Root / UUIDVar(id) / "username" => 
                getUserById(id, xa).flatMap { usernameOption =>
                    usernameOption match { 
                        case Some(username) => 
                            r.as[Json].flatMap { json =>
                                val newUsername = json.hcursor.get[String]("username").getOrElse("")
                                if(newUsername.nonEmpty) {
                                    changeUsername(id, newUsername, xa).flatMap { result =>
                                        Ok(s"Rows affected : $result")
                                    }
                                } else {
                                    BadRequest("Username must not be empty")
                                }
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

            

        }
    }
}

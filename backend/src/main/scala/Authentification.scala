import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityDecoder._

import cats.effect.IO
import cats.implicits._
import doobie.util.transactor.Transactor
import doobie.implicits._
import javax.xml.crypto.Data
import java.util.UUID
import doobie.util.meta.Meta
import scala.util.{Success, Failure}


import java.time.Instant
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import io.circe.Json
import io.circe.parser.decode
import io.circe.generic.auto._


import at.favre.lib.crypto.bcrypt.BCrypt

import User.addUser


object Authentification {
    // https://jwt-scala.github.io/jwt-scala/jwt-circe.html

    val key = "secretkey"
    val algo = JwtAlgorithm.HS256

    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    def generateToken(user_id: UUID): String = {
        val claim = JwtClaim(
            content = s"""{"user_id": "${user_id.toString}"}""",
            expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond),
            issuedAt = Some(Instant.now.getEpochSecond)
        )
        JwtCirce.encode(claim, key, algo)
    }

    def decodeToken(token: String): String = {
        val decodedToken = JwtCirce.decode(token, key, Seq(algo)) 
        decodedToken match {
            case Success(claim) =>
                val json = io.circe.parser.parse(claim.content).getOrElse(Json.Null)
                val userIdFromToken = json.hcursor.get[String]("user_id").getOrElse("")
                userIdFromToken
            case Failure(error) =>
                s"Error JWT: $error"
            }
    }

    // ça fait un peu redondant avec la UserRoute mais bon jsavais pas trop comment faire autrement
    // encrypter les passwords
    // la route fetch que le user en fonction de ses données donc elle login pas vraiment
    def loginUser(email: String, password: String, xa: Transactor[IO]): IO[Option[UUID]] = {
        
        sql"SELECT user_id, password FROM User WHERE email = $email LIMIT 1"
        .query[(UUID, String)]
        .option
        .transact(xa)
        .map {
            case Some((userId, hashedPassword)) =>
                // https://github.com/patrickfav/bcrypt/issues/16#issuecomment-486187182
                val passwordToCharArray = password.toCharArray
                val isSamePassword = BCrypt.verifyer().verify(passwordToCharArray, hashedPassword).verified
                if (isSamePassword) {
                    Some(userId)
                } else {
                    None
                }
            case None => None
        }
    }

    // delegue l'encryption du password a addUser
    def registerUser(username: String, email: String, password: String, xa: Transactor[IO]): IO[Int] = {
        val userInput = UserInput(username, password, email)
        addUser(userInput, xa)
    }

    def authentificationRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {
            
            case r @ POST -> Root / "register" =>
                r.as[Json].flatMap { json =>
                    
                    val username = json.hcursor.get[String]("username").getOrElse("")
                    val email = json.hcursor.get[String]("email").getOrElse("")
                    val password = json.hcursor.get[String]("password").getOrElse("")

                    registerUser(username, email, password, xa).flatMap { _ =>
                        Ok(Json.obj("message" -> Json.fromString("User registered successfully")))
                    }
                }

            case r @ POST -> Root / "login" =>
                r.as[Json].flatMap { json =>

                    val email = json.hcursor.get[String]("email").getOrElse("")
                    val password = json.hcursor.get[String]("password").getOrElse("")

                    loginUser(email, password, xa).flatMap {
                        case Some(user_id) =>
                            val token = generateToken(user_id)
                            Ok(Json.obj("message" -> Json.fromString("User connected"), "user_id" -> Json.fromString(user_id.toString), "token" -> Json.fromString(token)))
                        case None =>
                            Ok(Json.obj("error" -> Json.fromString("Invalid credentials")))
                    }
                }
        }
    }
}


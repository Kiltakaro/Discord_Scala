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
import scala.util.{Success, Failure}


import java.time.Instant
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import io.circe.Json


import at.favre.lib.crypto.bcrypt.BCrypt

import User.addUser


// ça commence a etre long dans user et dans guild
// on va gerer les connexion ici
// comme pour le PFE avec les JWT, c'était facile et pratique
// espérons la meme en scala
object Authentification {
    // https://jwt-scala.github.io/jwt-scala/jwt-circe.html

    val key = "secretkey"
    val algo = JwtAlgorithm.HS256


    // val userId = 88
    // val claim = JwtClaim(
    //     content = s"""{"userId": $userId}""",
    //     expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond),
    //     issuedAt = Some(Instant.now.getEpochSecond)
    // )
    // val token = JwtCirce.encode(claim, key, algo)

    // println(s"Token généré : $token")

    // val decoded = JwtCirce.decode(token, key, Seq(JwtAlgorithm.HS256))

    // println(s"Token décodé : $decoded")

    // val decodedjson = JwtCirce.decodeJson(token, key, Seq(JwtAlgorithm.HS256))

    // println(s"Token décodé en JSON : $decodedjson")

    // val jsonString = """{"name": "Peter", "age": 13, "pets": ["Toolkitty", "Scaniel"]}"""
    // val json: ujson.Value  = ujson.read(jsonString)
    // println(json("name").str)

    // val reponse: ujson.Value = ujson.read(decodedjson)
    // println(reponse("userId").str)

    // decodedjson match {
    //     case Success(json) =>
    //         val jsonString = json.noSpaces
    //         val reponse: ujson.Value = ujson.read(jsonString)
    //         println(reponse("userId").num.toInt)
    //         println(reponse("userId").num)
    //     case Failure(exception) =>
    //         println(s"exception : $exception")
    // }


    // def main(args: Array[String]): Unit = {
    //     println(s"Token généré : $token")
    //     println(s"Token décodé : $decoded")
    //     println(s"Token décodé en JSON : $decodedjson")
    // }


    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    def generateToken(userUUID: UUID): String = {
        val claim = JwtClaim(
            content = s"""{"userUUID": $userUUID}""",
            expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond),
            issuedAt = Some(Instant.now.getEpochSecond)
        )
        JwtCirce.encode(claim, key, algo)
    }

    // ça fait un peu redondant avec la UserRoute mais bon jsavais pas trop comment faire autrement
    
    // encrypter les passwords
    // la route fetch que le user en fonction de ses données donc elle login pas vraiment
    // A modifier pour Email psk en fait on peut avoir plusieurs usernames identiques
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

    // encrypter le password
    def registerUser(username: String, email: String, password: String, xa: Transactor[IO]): IO[Int] = {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray)
        val userInput = UserInput(username, hashedPassword, email)
        addUser(userInput, xa)
    }

    def authentificationRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {

            // case r @ POST -> Root / "register" =>
            //     r.as[String].flatMap { body =>

            //         println(s"Received body: $body")
            //         val json = ujson.read(body)
            //         println(s"json: $json")
            //         val username = json("username").str
            //         val password = json("password").str
            //         val username = json("username").str.getOrElse("")
            //         val password = json("password").str.getOrElse("")
            //         println(s" username: $username, password: $password")

            //         registerUser(username, password, xa).flatMap { userId =>
            //             Ok(ujson.Obj("message" -> "User registered successfully", "userId" -> userId).asJson)                }
            //     }     

            
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
                        case Some(userUUID) =>
                            val token = generateToken(userUUID)
                            Ok(Json.obj("message" -> Json.fromString("User connected"), "token" -> Json.fromString(token)))
                        case None =>
                            Ok(Json.obj("error" -> Json.fromString("Invalid credentials")))
                    }
                }
        }
    }
    
}


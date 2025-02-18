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
// METALS HURLE ICI WTF
// ALORS QUE TOUT MARCHE 
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import io.circe.Json



// PREUVE
// sbt "runMain Authentification"
// [info] welcome to sbt 1.10.7 (Ubuntu Java 21.0.6)
// [info] loading settings for project mini-discord-build-build from metals.sbt...
// [info] loading project definition from /home/Tanny/mini-discord/project/project
// [info] loading settings for project mini-discord-build from metals.sbt...
// [info] loading project definition from /home/Tanny/mini-discord/project
// [success] Generated .bloop/mini-discord-build.json
// [success] Total time: 2 s, completed Feb 12, 2025, 12:11:11 AM
// [info] loading settings for project mini-discord from build.sbt...
// [info] set current project to hello-world (in build file:/home/Tanny/mini-discord/)
// [info] compiling 1 Scala source to /home/Tanny/mini-discord/target/scala-3.3.1/classes ...
// [info] running Authentification 
// Token généré : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MzkzMjI2NzcsImlhdCI6MTczOTMxOTA3N30.-vZmM62IYxp4tq38fqRAo9xvYxvWnZei2UdWDBu9ACo
// Token décodé : Success(JwtClaim({}, None, None, None, Some(1739322677), None, Some(1739319077), None))
// Token décodé en JSON : Success({
//   "exp" : 1739322677,
//   "iat" : 1739319077
// })
// Token généré : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MzkzMjI2NzcsImlhdCI6MTczOTMxOTA3N30.-vZmM62IYxp4tq38fqRAo9xvYxvWnZei2UdWDBu9ACo
// Token décodé : Success(JwtClaim({}, None, None, None, Some(1739322677), None, Some(1739319077), None))
// Token décodé en JSON : Success({
//   "exp" : 1739322677,
//   "iat" : 1739319077
// })
// [success] Total time: 5 s, completed Feb 12, 2025, 12:11:17 AM

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
    def loginUser(username: String, password: String, xa: Transactor[IO]): IO[Option[UUID]] = {
        sql"SELECT user_id FROM User WHERE username = $username AND password = $password LIMIT 1"
        .query[UUID]
        .option
        .transact(xa)
    }

    // encrypter le password
    def registerUser(username: String, password: String, xa: Transactor[IO]): IO[Int] = {
        val userInput = UserInput(username, password, isAdmin = false)
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
                    val password = json.hcursor.get[String]("password").getOrElse("")

                    registerUser(username, password, xa).flatMap { _ =>
                        Ok(Json.obj("message" -> Json.fromString("User registered successfully")))
                    }
                }

            case r @ POST -> Root / "login" =>
                r.as[Json].flatMap { json =>

                    val username = json.hcursor.get[String]("username").getOrElse("")
                    val password = json.hcursor.get[String]("password").getOrElse("")

                    loginUser(username, password, xa).flatMap {
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


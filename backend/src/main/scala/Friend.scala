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

case class FriendInput(userUUID : String, friendUUID: String)

object Friend {


    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)

    def sendFriendRequest(friendInput: FriendInput, xa: Transactor[IO]): IO[Int] = {
        val insertFriendRequest =
        sql"""
            INSERT INTO Friends (friendship_id, user_id1, user_id2, request_accepted)
            VALUES (generateUUIDv4(), ${friendInput.userUUID}, ${friendInput.friendUUID}, 0)
        """.update.run
        insertFriendRequest.transact(xa)
    }



    def friendRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {

            case r @ POST -> Root / "add" =>
                r.as[FriendInput].attempt.flatMap {
                    case Right(friendInput) =>
                        if (friendInput.userUUID.nonEmpty && friendInput.friendUUID.nonEmpty) {
                            sendFriendRequest(friendInput, xa).flatMap { result =>
                                Ok(s"Rows affected: $result")
                            }
                        } else {
                            BadRequest("IDs must not be empty")
                        }
                    case Left(_) =>
                        BadRequest("Bad Request. Format { userUUID: String, friendUUID: String }")
                    }

        }
    }
    
}


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

case class FriendRequestInput(userUUID : String, friendUUID: String)

case class FriendRequestOutput(username: String, userUUID: String)


object Friend {

    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)

    def sendFriendRequest(friendRequestInput: FriendRequestInput, xa: Transactor[IO]): IO[Int] = {
        val insertFriendRequest =
        sql"""
            INSERT INTO Friends (friendship_id, user_id1, user_id2, request_accepted)
            VALUES (generateUUIDv4(), ${friendRequestInput.userUUID}, ${friendRequestInput.friendUUID}, 0)
        """.update.run
        insertFriendRequest.transact(xa)
    }

    // je viens de penser mais de cette façon la personne qui reçoit la demande est forcément le user2
    // donc on aurait pu nommé user_id2 genre asked_friend ou un truc du genre
    def getFriendRequestsUsernames(userUUID: UUID, xa: Transactor[IO]): IO[List[(FriendRequestOutput)]] = {
        sql"""
            SELECT username, user_id1 FROM 
            Friends JOIN User ON Friends.user_id1 = User.user_id
            WHERE user_id2 = ${userUUID.toString} AND request_accepted = 0
        """
        .query[FriendRequestOutput]
        .to[List]
        .transact(xa)
    }


    def friendRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {

            case r @ POST -> Root / "add" =>
                r.as[FriendRequestInput].attempt.flatMap {
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
            
            // pour afficher les requests "en attente" et de qui elles viennent
            case GET -> Root / "requests" / UUIDVar(uuid) =>
                getFriendRequestsUsernames(uuid, xa).flatMap { requests =>
                    Ok(requests.asJson)
                }

        }
    }
    
}


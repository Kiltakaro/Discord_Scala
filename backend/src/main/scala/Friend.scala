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
import org.typelevel.ci._


case class FriendRequestInput(user_id : String, friend_id: String)

case class FriendRequestOutput(username: String, user_id: String)


object Friend {

    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)

    def sendFriendRequest(user_id: UUID, friend_id: UUID, xa: Transactor[IO]): IO[Int] = {
        if (user_id == friend_id) {
            IO.pure(0)
        } else {
            val checkExisting =
            sql"""
                SELECT count() FROM Friends
                WHERE (user_id1 = ${user_id.toString} AND user_id2 = ${friend_id.toString})
                OR (user_id1 = ${friend_id.toString} AND user_id2 = ${user_id.toString})
            """.query[Int].unique

            val insertFriendRequest =
            sql"""
                INSERT INTO Friends (friendship_id, user_id1, user_id2, request_accepted)
                VALUES (generateUUIDv4(), ${user_id.toString}, ${friend_id.toString}, 0)
            """.update.run

            for {
                exists <- checkExisting.transact(xa)
                result <- if (exists > 0) IO.pure(0) else insertFriendRequest.transact(xa)
            } yield result
        }
    }

    // la personne qui reçoit la demande est forcément le user2
    def getFriendRequestsUsernames(user_id: UUID, xa: Transactor[IO]): IO[List[(FriendRequestOutput)]] = {
        sql"""
            SELECT username, user_id1 FROM 
            Friends JOIN User ON Friends.user_id1 = User.user_id
            WHERE user_id2 = ${user_id.toString} AND request_accepted = 0
        """
        .query[FriendRequestOutput]
        .to[List]
        .transact(xa)
    }

    def declineFriendRequest(user_id: UUID, friend_id: UUID, xa: Transactor[IO]): IO[Int] = {
        // le user actuel est celui qui a reçu la demande => user2
        sql"""
            DELETE FROM Friends WHERE 
            user_id1 = ${friend_id.toString} AND user_id2 = ${user_id.toString}
        """.update.run
        .transact(xa)
    }

    def acceptFriendRequest(user_id: UUID, friend_id: UUID, xa: Transactor[IO]): IO[Int] = {
        // le user actuel est celui qui a reçu la demande => user2
        sql"""
            UPDATE Friends SET request_accepted = 1 WHERE 
            user_id1 = ${friend_id.toString} AND user_id2 = ${user_id.toString}
        """.update.run
        .transact(xa)
    }

    
    def getFriends(user_id: UUID, xa: Transactor[IO]): IO[List[FriendRequestOutput]] = {
        // user actuel peut etre user1 ou user2 faut check les 2
        sql"""
            SELECT username, user_id2 FROM Friends 
            INNER JOIN User ON Friends.user_id2 = User.user_id 
            WHERE user_id1 =  $user_id AND request_accepted = 1 

            UNION ALL

            SELECT username, user_id1 FROM Friends 
            INNER JOIN User ON Friends.user_id1 = User.user_id 
            WHERE user_id2 = $user_id AND request_accepted = 1
        """
        .query[FriendRequestOutput]
        .to[List]
        .transact(xa)
    }

    // contrairement aux autres querries, la on sait pas si c'est user1 ou 2
    def getFriendshipIdFromUsers(user_id1: UUID, user_id2: UUID, xa: Transactor[IO]): IO[Option[UUID]] = {
        sql"""
            SELECT friendship_id FROM Friends
            WHERE (user_id1 = ${user_id1.toString} AND user_id2 = ${user_id2.toString})
            OR (user_id1 = ${user_id2.toString} AND user_id2 = ${user_id1.toString})
        """.query[UUID].option
        .transact(xa)
    }

    def friendRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {


            case r @ POST -> Root / "add" =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        r.as[Json].flatMap { json =>
                            val friendId = json.hcursor.get[String]("friend_id").getOrElse("")
                            sendFriendRequest(UUID.fromString(userIdFromToken), UUID.fromString(friendId), xa).flatMap { result =>
                                Ok(result.asJson)
                            }
                        }
                    case None =>
                        BadRequest("Token not found")
                }
            

            case r @ GET -> Root / "requests" =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        getFriendRequestsUsernames(UUID.fromString(userIdFromToken), xa).flatMap { result =>
                            Ok(result.asJson)
                        }
                    case None =>
                        BadRequest("Token not found")
                }


            // pourrait etre une route delete
            case r @ POST -> Root / "decline" =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")
                        val userIdFromToken = Authentification.decodeToken(token)
                        r.as[Json].flatMap { json =>
                            val friendId = json.hcursor.get[String]("friend_id").getOrElse("")
                            declineFriendRequest(UUID.fromString(userIdFromToken), UUID.fromString(friendId), xa).flatMap { result =>
                                Ok(s"Rows affected: $result")
                            }
                        }
                    case None =>
                        BadRequest("Token not found")
                }


            case r @ POST -> Root / "accept" =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")
                        val userIdFromToken = Authentification.decodeToken(token)
                        r.as[Json].flatMap { json =>
                            val friendId = json.hcursor.get[String]("friend_id").getOrElse("")
                            acceptFriendRequest(UUID.fromString(userIdFromToken), UUID.fromString(friendId), xa).flatMap { result =>
                                Ok(s"Rows affected: $result")
                            }
                        }
                    case None =>
                        BadRequest("Token not found")
                }


            case r @ GET -> Root =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        getFriends(UUID.fromString(userIdFromToken), xa).flatMap { result =>
                            Ok(result.asJson)
                        }
                    case None =>
                        BadRequest("Token not found")
                }

            case r @ DELETE -> Root / UUIDVar(friendId) =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")
                        val userIdFromToken = Authentification.decodeToken(token)
                        declineFriendRequest(UUID.fromString(userIdFromToken), friendId, xa).flatMap { result =>
                            Ok(s"Rows affected: $result")
                    }
                    case None =>
                        BadRequest("Token not found")
                }

            // Recupere friendship_id pour trouver le channel associé
            case r @ GET -> Root / UUIDVar(friendId) =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        getFriendshipIdFromUsers(UUID.fromString(userIdFromToken), friendId, xa).flatMap { result =>
                            Ok(result.asJson)
                        }
                    case None =>
                        BadRequest("Token not found")
                }

        }
    }
    
}


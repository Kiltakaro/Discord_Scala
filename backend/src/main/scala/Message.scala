import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import cats.effect.IO
import doobie.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax.*
import io.circe.Json
import io.circe.parser._
import scala.util.Try
import org.typelevel.ci._
import org.http4s.circe.CirceEntityDecoder._

import java.util.UUID


case class MessageOutput(message_id: UUID, channel_id: UUID, sender_id:UUID, content: String, sent_at: String, username: String)

object Message {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)

    ///////////////////////// MESSAGES //////////////////////////////////////


    def getMessagesFromChannel(channel_id: UUID, xa: Transactor[IO]): IO[List[MessageOutput]] = {
        sql"""
            SELECT msg.message_id, msg.channel_id, msg.sender_id, msg.content, msg.sent_at, user.username
            FROM Message msg
            JOIN User user
            ON msg.sender_id = user.user_id
            WHERE msg.channel_id = $channel_id
            ORDER BY msg.sent_at ASC
        """.query[MessageOutput].to[List].transact(xa)
    }

    def deleteMessage(channelId: UUID, messageId: UUID, xa: Transactor[IO]): IO[Int] = {
        sql"""
            DELETE FROM Message
            WHERE message_id = ${messageId} AND channel_id = ${channelId}
        """.update.run.transact(xa)
    }


    def messageRoutes(xa: Transactor[IO]): HttpRoutes[IO] = {
            HttpRoutes.of[IO] {

            case req @ GET -> Root / "channel" / UUIDVar(channelId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")
                        
                        val userIdFromToken = Authentification.decodeToken(token)
                            getMessagesFromChannel(channelId, xa).flatMap { messages =>
                                Ok(messages.asJson)
                            }
                    case None => BadRequest("Token not found")
                }
            

            case req @ POST -> Root / "channel" / UUIDVar(channelId) / "send" =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")
                        
                        val userIdFromToken = Authentification.decodeToken(token)

                        req.as[Json].flatMap { json =>
                            val content = json.hcursor.get[String]("content").getOrElse("")

                            val message = MessageInput(
                                channel_id = channelId, 
                                sender_id = UUID.fromString(userIdFromToken),
                                content = content,
                                sent_at = java.time.Instant.now.toString
                            )
                            MessageProducer.sendMessage(message) *> Ok("Message sent")
                        }
                        
                    case None => BadRequest("Token not found")
                }

            case req @ DELETE -> Root / UUIDVar(messageId) / "channel" / UUIDVar(channelId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")
                        
                        val userIdFromToken = Authentification.decodeToken(token)
                        // ajouter verif si admin
                        deleteMessage(channelId, messageId, xa).flatMap {
                            case 0 =>
                                NotFound(s"Pas de message trouvé avec l'ID $messageId dans le channel $channelId")

                            case result@1 =>
                                Ok(s"Rows affected : $result")
                        }

                    case None => BadRequest("Token not found")
                }

        }
    }
}
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

case class ChannelInput(name: String, friendshipId: Option[UUID], guildId: Option[UUID])
case class ChannelOutput(channelId: UUID, name: String, friendshipId: Option[String], guildId: Option[String])

object Channel {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)

    def getGuildChannels(guildId: UUID, xa: Transactor[IO]): IO[List[ChannelOutput]] = {
        sql"""
            SELECT * FROM Channel
            WHERE guild_id = $guildId
        """.query[ChannelOutput].to[List].transact(xa)
    }

    // Limit 1 psk a force de faire des tests j'ai dupliqué le channel DM et ça faisait tout planter
    def getDMChannel(friendshipId: UUID, xa: Transactor[IO]): IO[Option[ChannelOutput]] = {
        sql"""
            SELECT * FROM Channel
            WHERE friendship_id = $friendshipId
            limit 1
        """.query[ChannelOutput].option.transact(xa)
    }

    // Permet de créer un channel DM ou guild, suffit de mettre NULL à friendshipId ou à guildId en fonction de ce qu'on veut
    def createChannel(channel: ChannelInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            INSERT INTO Channel (channel_name, friendship_id, guild_id)
            VALUES (${channel.name}, ${channel.friendshipId}, ${channel.guildId})
        """.update.run.transact(xa)
    }

    // Faut voir si on ajoute des trucs à modifier pour un channel, pour le moment il n'y a que le nom de modifiable
    def updateChannel(channelId: UUID, name: String, xa: Transactor[IO]): IO[Int] = {
        sql"""
            ALTER TABLE Channel
            UPDATE channel_name = $name
            WHERE channel_id = $channelId
        """.update.run.transact(xa)
    }

    def deleteChannel(channelId: UUID, xa: Transactor[IO]): IO[Int] = {
        sql"""
            DELETE FROM Channel
            WHERE channel_id = ${channelId}
        """.update.run.transact(xa)
    }

    // ROUTES
    def channelRoutes(xa: Transactor[IO]): HttpRoutes[IO] = {
        HttpRoutes.of[IO] {
            case req@GET -> Root / "guilds" / UUIDVar(guildId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        getGuildChannels(guildId, xa).flatMap { channelList =>
                            Ok(channelList.asJson)
                        }

                    case None =>
                        BadRequest("Token not found")
                }

            case req@POST -> Root / "guilds" / UUIDVar(guildId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        req.as[ChannelInput].attempt.flatMap {
                            case Right(channel: ChannelInput) =>
                                if (channel.name.isEmpty) {
                                    BadRequest("Channel name must not be empty")
                                }
                                else {
                                    createChannel(channel, xa).flatMap { result =>
                                        Ok(s"Rows affected: $result")
                                    }
                                }
                            case Left(_) =>
                                BadRequest("Bad request format, expected {name: String, friendship_id: Option[UUID], guild_id: Option[UUID]")
                        }

                    case None =>
                        BadRequest("Token not found")
                }

            case req@DELETE -> Root / UUIDVar(channelId) / "guilds" / UUIDVar(guildId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        // ajouter verif si admin
                        deleteChannel(channelId, xa).flatMap {
                            case 0 =>
                                NotFound(s"Pas de channel trouvé avec l'ID $channelId")

                            case result@1 =>
                                Ok(s"Rows affected : $result")
                        }

                    case None =>
                        BadRequest("Token not found")
                }

            ////////////////////////// POUR LES DM ///////////////////////////

            case req@GET -> Root / "friends" / UUIDVar(friendshipId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        getDMChannel(friendshipId, xa).flatMap { channel =>
                            Ok(channel.asJson)
                        }

                    case None =>
                        BadRequest("Token not found")
                }

            case req@POST -> Root / "friends" / UUIDVar(friendshipId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        // faudrait hypothétiquement vérifier si le userIdFromToken est bien ami avec le friendshipId
                        val channelDM = ChannelInput(
                            name = "DM",
                            friendshipId = Some(friendshipId), // sinon erreur car c'est une option
                            guildId = None // il faut le mettre a none car c'est une option
                        )
                        createChannel(channelDM, xa).flatMap { result =>
                            Ok(s"Rows affected: $result")
                        }

                    case None =>
                        BadRequest("Token not found")
                }
            

            case req@DELETE -> Root / UUIDVar(channelId)  =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        deleteChannel(channelId, xa).flatMap {
                            case 0 =>
                                NotFound(s"Pas de channel trouvé avec l'ID $channelId")

                            case result@1 =>
                                Ok(s"Rows affected : $result")
                        }

                    case None =>
                        BadRequest("Token not found")
                }

            
        }
    }
}    
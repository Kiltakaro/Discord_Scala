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

case class ChannelModel(name: String, friendshipId: Option[UUID], guildId: Option[UUID])
case class ChannelOutputModel(channelId: UUID, name: String, friendshipId: Option[String], guildId: Option[String])

object Channel {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)

    def getGuildChannels(guildId: UUID, xa: Transactor[IO]): IO[List[ChannelOutputModel]] = {
        sql"""
            SELECT * FROM Channel
            WHERE guild_id = $guildId
        """.query[ChannelOutputModel].to[List].transact(xa)
    }

    // Permet de créer un channel DM ou guild, suffit de mettre NULL à friendshipId ou à guildId en fonction de ce qu'on veut
    def createChannel(channel: ChannelModel, xa: Transactor[IO]): IO[Int] = {
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
            case req@GET -> Root / UUIDVar(guildId) =>
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

            case req@POST -> Root / UUIDVar(guildId) =>
                req.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        req.as[ChannelModel].attempt.flatMap {
                            case Right(channel: ChannelModel) =>
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

            case req@DELETE -> Root / UUIDVar(guildId) / UUIDVar(channelId) =>
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
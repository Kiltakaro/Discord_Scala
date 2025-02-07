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


// ça sert a rien pour le moment
case class Guild(guild_name: String)

// pourquoi s'embeter avec ça, j'ai pas l'impression que ce soit super utile ici
case class GuildInput(guild_name: String, owner_id : UUID)


// AJOUTER GET ALL USERS IN GUILD
// il faut add in the array of guild_members

object Guild {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    def addGuild(guild: GuildInput, xa: Transactor[IO]): IO[Int] = {
        val insertGuild =
        sql"""
            INSERT INTO Guild (guild_name, owner_id)
            VALUES (${guild.guild_name}, ${guild.owner_id})
        """.update.run
        insertGuild.transact(xa)
    }

    // Récupère tous les Guild
    // faudra peut etre récup l'id OU le nom du owner
    def getAllGuilds(xa: Transactor[IO]): IO[List[(UUID, String)]] = {
        val query = sql"SELECT guild_id, guild_name FROM Guild".query[(UUID, String)]
        val queryToList: doobie.ConnectionIO[List[(UUID, String)]] = query.to[List]
        queryToList.transact(xa)
    }

    def getGuildById(id: UUID, xa: Transactor[IO]): IO[Option[(UUID, String)]]= {
        sql"SELECT guild_id, guild_name FROM Guild WHERE guild_id = $id"
        .query[(UUID, String)]
        .option
        .transact(xa)
    }

    def deleteGuild(id: UUID, xa: Transactor[IO]): IO[Int] = {
        val deleteGuild = sql"DELETE FROM Guild WHERE guild_id = $id"
        .update
        .run
        deleteGuild.transact(xa)
     }


    // PLUS BESOIN DE METTRE Guild DANS LA ROUTE CAR IL EST DANS LE ROUTEUR
    def guildRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {
            
            // Pour mettre un String dans une route
            case GET -> Root / "hello" / name =>
                Ok(s"Hello, $name.")
            
            
            // READ
            // Attention la requête c'est /Guilds/<uuid> et pas /Guilds?id=<uuid>, ça peut porter à confusion l'id n'est pas un paramètre
            case GET -> Root / UUIDVar(id) =>
                getGuildById(id, xa).flatMap { 
                    guildOption => 
                        guildOption match {
                        case Some((id, guild_name)) => 
                            Ok((id, guild_name).asJson)

                        case None =>
                            NotFound(s"No Guild with ID : $id")

                        }
                }
            
            // Recup tous les Guild
            case GET -> Root =>
                getAllGuilds(xa).flatMap { guilds => 
                    Ok(guilds.asJson)
                }

        

            // CREATE
            case r @ POST -> Root / "create" =>
                r.as[GuildInput].attempt.flatMap {
                    case Right(guild: GuildInput) =>
                        if (guild.guild_name.length > 0) {
                            addGuild(guild, xa).flatMap { result =>
                                Ok(s"rows affected : $result")
                            }
                        }
                        else {
                            BadRequest("Name must be longer")
                        }
                    case Left(_) =>
                        BadRequest("Error format {guild_name: String, owner_id : UUID}")
                }
            

            // Supprime une Guild
            case DELETE -> Root / UUIDVar(id) =>
                getGuildById(id, xa).flatMap {
                    guildOption =>
                        guildOption match {

                            case Some(guild) => 
                                deleteGuild(id, xa).flatMap {
                                    result =>
                                        Ok(s"Affected rows : $result")
                                }

                            case None => 
                                NotFound(s"Couldn't delete Guild with id $id : not found")
                        }

                }

            
            // Route not found à laisser
            case GET -> Root / _ =>
                NotFound("Guild Route Not Found")
            }
        }
}

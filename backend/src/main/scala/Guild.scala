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

case class GuildInviteInput(userUUID: String, guildUUID: String)
case class GuildInviteOutput(guildUUID: String, guild_name: String)


// https://rockthejvm.com/articles/learning-doobie-for-the-greater-good
object Guild {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    //////////////////////// GUILD ITSELF //////////////////////////


    def addGuild(guild: GuildInput, xa: Transactor[IO]): IO[Int] = {
        val insertGuild =
        sql"""
            INSERT INTO Guild (guild_name, owner_id)
            VALUES (${guild.guild_name}, ${guild.owner_id})
        """.update.run
        insertGuild.transact(xa)
    }

    // Crée une guilde et y ajoute son créateur
    def createGuild(guildName: String, guildDescription: String, ownerId: UUID, xa: Transactor[IO]): IO[UUID] = {
        val guildId = UUID.randomUUID()
        for {
            _ <- sql"""
                INSERT INTO Guild (guild_id, guild_name, guild_description, owner_id, creation_date)
                VALUES ($guildId, $guildName, $guildDescription, $ownerId, now())
            """.update.run.transact(xa)
            _ <- sql"""
                INSERT INTO User_Guild (user_id, guild_id) VALUES ($ownerId, $guildId)
            """.update.run.transact(xa)
        } yield guildId
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

    /////////////////////////// GUILD ATTRIBUTS ///////////////////////////


    def getGuildDesc(id: UUID, xa: Transactor[IO]): IO[Option[String]] = {
        sql"SELECT guild_description FROM Guild WHERE guild_id = $id"
        .query[String]
        .option
        .transact(xa)
    }

    def modifyGuildDesc(id: UUID, guild_description: String, xa: Transactor[IO]): IO[Int] = {
        val modifyGuild = sql"UPDATE Guild SET guild_description = $guild_description WHERE guild_id = $id"
        .update
        .run
        modifyGuild.transact(xa)
    }

    def getGuildName(id: UUID, xa: Transactor[IO]): IO[Option[String]] = {
        sql"SELECT guild_name FROM Guild WHERE guild_id = $id"
        .query[String]
        .option
        .transact(xa)
    }

    def modifyGuildName(id: UUID, guild_name: String, xa: Transactor[IO]): IO[Int] = {
        val modifyGuild = sql"UPDATE Guild SET guild_name = $guild_name WHERE guild_id = $id"
        .update
        .run
        modifyGuild.transact(xa)
    }


    def getGuildOwnerId(id: UUID, xa: Transactor[IO]): IO[Option[UUID]] = {
        sql"SELECT owner_id FROM Guild WHERE guild_id = $id"
        .query[UUID]
        .option
        .transact(xa)
    }


    // Faudra rajouter un moyen de récuperer directement le nom pour l'affichage ça sera utile


    def modifyGuildOwner(id: UUID, owner_id: UUID, xa: Transactor[IO]): IO[Int] = {
        val modifyGuild = sql"UPDATE Guild SET owner_id = $owner_id WHERE guild_id = $id"
        .update
        .run
        modifyGuild.transact(xa)
    }

    /////////////////////////// GUILD RELATIONS ///////////////////////////

    // Users
    def addUserToGuild(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Int] = {
        val addUser = sql"INSERT INTO User_Guild (user_id, guild_id) VALUES ($userId, $guildId)"
        .update
        .run
        addUser.transact(xa)
    }

    def getUsersInGuild(id: UUID, xa: Transactor[IO]): IO[List[UUID]] = {
        val getUsers: doobie.ConnectionIO[List[UUID]] = sql"SELECT user_id FROM User_Guild WHERE guild_id = $id"
        .query[UUID]
        .to[List]
        getUsers.transact(xa)
    }

    def removeUserFromGuild(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Int] = {
        val removeUser = sql"DELETE FROM User_Guild WHERE user_id = $userId AND guild_id = $guildId"
        .update
        .run
        removeUser.transact(xa)
    }
    
    /////////////////////////// GUILD INVITES #1 ///////////////////////////

    // Version individuelle : même système que les demandes d'ami

    def sendGuildInvite(guildInviteInput: GuildInviteInput, xa: Transactor[IO]): IO[Int] = {
        val insertGuildInvite =
        sql"""
            INSERT INTO User_Guild (user_id, guild_id, invite_accepted)
            VALUES (${guildInviteInput.userUUID}, ${guildInviteInput.guildUUID}, 0)
        """.update.run
        insertGuildInvite.transact(xa)
    }

    def getGuildInvitesGuildnames(userUUID: UUID, xa: Transactor[IO]): IO[List[(GuildInviteOutput)]] = {
        sql"""
            SELECT guild_id, guild_name FROM 
            User_Guild JOIN Guild ON User_Guild.guild_id = Guild.guild_id
            WHERE user_id = ${userUUID.toString} AND invite_accepted = 0
        """
        .query[GuildInviteOutput]
        .to[List]
        .transact(xa)
    }

    def declineGuildInvite(guildInviteInput :GuildInviteInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            DELETE FROM User_Guild WHERE 
            user_id = ${guildInviteInput.userUUID} AND guild_id = ${guildInviteInput.guildUUID}
        """.update.run
        .transact(xa)
    }

    def acceptGuildInvite(guildInviteInput: GuildInviteInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            UPDATE User_Guild SET invite_accepted = 1 WHERE 
            user_id = ${guildInviteInput.userUUID} AND user_id2 = ${guildInviteInput.guildUUID}
        """.update.run
        .transact(xa)
    }


    //////////////////////// GUILD INVITES #2 //////////////////////////

    // Version communautaire : avec des liens d'invitations (pour plus tard car nécessite la mise en place de la messagerie)

    // def generateInviteCode(): String = {
    //     val randomBytes = new Arrayrate a short 6-byte random string
    //     Random.nextBytes(randomBytes)
    //     Base64.getUrlEncoder.withoutPadding().encodeToString(randomBytes)
    // }

    // def createGuildInvite(guildId: UUID, creatorId: UUID, maxUses: Int, xa: Transactor[IO]): IO[String] = {
    //     val inviteCode = generateInviteCode()
    //     val expirationTime = Instant.now().plusSeconds(3600) // valide une heure (je rajouterai le cas Unlimited plus tard)

    //     sql"""
    //         INSERT INTO Guild_Invites (invite_code, guild_id, creator_id, max_uses, expires_at)
    //         VALUES ($inviteCode, $guildId, $creatorId, $maxUses, $expirationTime)
    //     """.update.run.transact(xa).map(_ => inviteCode)
    // }

    // def getInvite(inviteCode: String, xa: Transactor[IO]): IO[Option[(UUID, Int, Int, Instant)]] = {
    //     sql"""
    //         SELECT guild_id, max_uses, uses, expires_at 
    //         FROM Guild_Invites 
    //         WHERE invite_code = $inviteCode
    //     """.query[(UUID, Int, Int, Instant)].option.transact(xa)
    // }

    // def joinGuildUsingInvite(inviteCode: String, userId: UUID, xa: Transactor[IO]): IO[Either[String, String]] = {
    //     getInvite(inviteCode, xa).flatMap {
    //         case Some((guildId, maxUses, uses, expiresAt)) =>
    //             val now = Instant.now()
    //             if (now.isAfter(expiresAt)) {
    //                 IO.pure(Left("Invite expired"))
    //             } else if (maxUses > 0 && uses >= maxUses) {
    //                 IO.pure(Left("Invite unavailable"))
    //             } else {
    //                 sql"""
    //                     UPDATE Guild_Invites SET uses = uses + 1 WHERE invite_code = $inviteCode
    //                 """.update.run.transact(xa) *>

    //                 // Insert à remplacer par addUserToGuild() une fois testé
    //                 sql"""
    //                     INSERT INTO User_Guild (user_id, guild_id) VALUES ($userId, $guildId)
    //                 """.update.run.transact(xa).map(_ => Right("Successfully joined the guild"))
    //             }
            
    //         case None => IO.pure(Left("Invalid invite code"))
    //     }
    // }

    /////////////////////////// GUILD ROUTES ///////////////////////////

    // PLUS BESOIN DE METTRE Guild DANS LA ROUTE CAR IL EST DANS LE ROUTEUR
    def guildRoutes(xa: Transactor[IO])= {
        HttpRoutes.of[IO] {
            
            // Pour mettre un String dans une route
            case GET -> Root / "hello" / name =>
                Ok(s"Hello, $name.")
            
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
            // Version expérimentale
            // case req @ POST -> Root / "create" =>
            //     req.as[Json].flatMap { json =>
            //         val guildName = json.hcursor.get[String]("guildName").getOrElse("")
            //         val guildDescription = json.hcursor.get[String]("guildDescription").getOrElse("")
            //         val ownerId = UUID.fromString(json.hcursor.get[String]("ownerId").getOrElse(""))

            //         if (guildName.nonEmpty) {
            //             createGuild(guildName, guildDescription, ownerId, xa).flatMap { guildId =>
            //                 Ok(Json.obj("message" -> Json.fromString("Serveur créé !"), "guildId" -> Json.fromString(guildId.toString)))
            //             }
            //         } else {
            //             BadRequest(Json.obj("error" -> Json.fromString("Le nom du serveur ne peut pas être vide.")))
            //         }
            //     }

            
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

            
            // Récup tous les serveurs d'un user (WIP je sais pas comment récup / utiliser un array clickhouse en scala)
            case GET -> Root / UUIDVar(id) / "users" =>
                getUsersInGuild(id, xa).flatMap { users =>
                    Ok(users.asJson)
                }
            
            // UPDATE ??


            //// DELETE
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

            
            ///////////////////////////// GESTION DES INVITATIONS ///////////////////: 


            case r @ POST -> Root / "invites" / "add" =>
                r.as[GuildInviteInput].attempt.flatMap {
                    case Right(guildInput) =>
                        if (guildInput.userUUID.nonEmpty && guildInput.guildUUID.nonEmpty) {
                            sendGuildInvite(guildInput, xa).flatMap { result =>
                                Ok(s"Rows affected: $result")
                            }
                        } else {
                            BadRequest("IDs must not be empty")
                        }
                    case Left(_) =>
                        BadRequest("Bad Request. Format { userUUID: String, friendUUID: String }")
                    }

            
            // pour afficher les requests "en attente" et de qui elles viennent
            case GET -> Root / "invites" / UUIDVar(uuid) =>
                getGuildInvitesGuildnames(uuid, xa).flatMap { invites =>
                    Ok(invites.asJson)
                }


            // Refuse une friend request
            // j'hésite a en faire une route DELETE  
            // psk techniquement, ça fait supprimer un truc
            case r @ POST -> Root / "invites" / "decline" =>
                r.as[GuildInviteInput].attempt.flatMap {
                        case Right(guildInput) =>
                            if (guildInput.userUUID.nonEmpty && guildInput.guildUUID.nonEmpty) {
                                declineGuildInvite(guildInput, xa).flatMap { result =>
                                    Ok(s"Rows affected: $result")
                                }
                            } else {
                                BadRequest("IDs must not be empty")
                            }
                        case Left(_) =>
                            BadRequest("Bad Request. Format { userUUID: String, friendUUID: String }")
                }


            case r @ POST -> Root / "invites" / "accept" =>
                r.as[GuildInviteInput].attempt.flatMap {
                    case Right(guildInput) =>
                        if (guildInput.userUUID.nonEmpty && guildInput.guildUUID.nonEmpty) {
                            acceptGuildInvite(guildInput, xa).flatMap { result =>
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

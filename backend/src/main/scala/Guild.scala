import cats.effect.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.Json
import io.circe.parser._
import pdi.jwt._
import org.http4s.{dsl, *}
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.Challenge
import cats.data.NonEmptyList
import org.typelevel.ci.CIStringSyntax

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

case class GuildInviteInput(user_id: String, guild_id: String)
case class GuildInviteOutput(guild_id: String, guild_name: String)


// https://rockthejvm.com/articles/learning-doobie-for-the-greater-good
object Guild {
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    //////////////////////// GUILD ITSELF //////////////////////////


    // Crée une guilde et y ajoute son créateur
    def createGuild(guildName: String, guildDescription: String, ownerId: UUID, xa: Transactor[IO]): IO[UUID] = {
        val guildId = UUID.randomUUID()
        for {
            _ <- sql"""
                INSERT INTO Guild (guild_id, guild_name, guild_description, owner_id, creation_date)
                VALUES ($guildId, $guildName, $guildDescription, $ownerId, now())
            """.update.run.transact(xa)
            _ <- sql"""
                INSERT INTO User_Guild (user_id, guild_id, invite_accepted) VALUES ($ownerId, $guildId, 1)
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

    def getGuildById(id: UUID, xa: Transactor[IO]): IO[Option[(UUID, String, String, UUID)]]= {
        sql"SELECT guild_id, guild_name, guild_description, owner_id FROM Guild WHERE guild_id = $id"
        .query[(UUID, String, String, UUID)]
        .option
        .transact(xa)
    }

    def deleteGuild(id: UUID, xa: Transactor[IO]): IO[Int] = {
        for {
            userRows <- sql"""
                DELETE FROM User_Guild WHERE guild_id = $id
            """.update.run.transact(xa)
            _ <- sql"""
                DELETE FROM Channels WHERE guild_id = $id
            """.update.run.transact(xa)
            _ <- sql"""
                DELETE FROM Guild WHERE guild_id = $id
            """.update.run.transact(xa)
        } yield userRows
    }

    // def deleteGuild(id: UUID, xa: Transactor[IO]) = {
    //     val deleteUser = sql"DELETE FROM Guild WHERE guild_id = $id"
    //     .update
    //     .run
    //     deleteUser.transact(xa)
    // }

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

    // Récupère l'ID de l'utilisateur connecté via son token JWT (pour l'instant ne fonctionne pas)
    def getUserIdFromJWT(token: String): Option[UUID] = {
        println(s"Received Token: $token")
        JwtCirce.decode(token, "secretkey", Seq(JwtAlgorithm.HS256)).toOption.flatMap { decoded =>
            parse(decoded.content).toOption.flatMap { json =>
                json.hcursor.get[String]("user_id").toOption.map(UUID.fromString)
            }
        }  
    }

    def addUserToGuild(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Int] = {
        val addUser = sql"INSERT INTO User_Guild (user_id, guild_id) VALUES ($userId, $guildId)"
        .update
        .run
        addUser.transact(xa)
    }


    def getUsersInGuild(id: UUID, xa: Transactor[IO]): IO[List[UserOutput]] = {
        val getUsers = sql"""
        SELECT user_id, username FROM 
        User_Guild JOIN User ON User_Guild.user_id = User.user_id
        WHERE guild_id = ${id.toString} AND invite_accepted = 1
        """
        .query[UserOutput]
        .to[List]
        getUsers.transact(xa)
    }

    def removeUserFromGuild(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Int] = {
        val removeUser = sql"DELETE FROM User_Guild WHERE user_id = $userId AND guild_id = $guildId"
        .update
        .run
        removeUser.transact(xa)
    }

    // Pour savoir si un user est dans une guild : 0 = le user n'y est pas, 1 = le user y est
    def checkIfUserIsInGuild(guildId: UUID, userId: UUID, xa: Transactor[IO]): IO[Int] = {
        sql"""
          SELECT count() FROM User_Guild
          WHERE (user_id = $userId) AND (guild_id = $guildId)
        """.query[Int].unique.transact(xa)
    }


    /////////////////////////// BANS ///////////////////////////

    def banUserFromGuild(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[UUID] = {
        for {
            _ <- sql"INSERT INTO Guild_Ban (user_id, guild_id) VALUES ($userId, $guildId)"
              .update
              .run
              .transact(xa)

            _ <- sql"DELETE FROM User_Guild WHERE user_id = $userId AND guild_id = $guildId"
              .update
              .run
              .transact(xa)
        } yield userId
    }

    def checkIfUserIsBanned(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Int] = {
        sql"""
             SELECT count() FROM Guild_Ban
             WHERE (guild_id = $guildId) AND (user_id = $userId)
        """.query[Int].unique.transact(xa)
    }

    def getBannedUsers(guildId: UUID, xa: Transactor[IO]): IO[List[UserOutput]] = {
        sql"""
        SELECT User.user_id, User.username FROM User
        JOIN Guild_Ban ON (Guild_Ban.user_id = User.user_id)
        WHERE Guild_Ban.guild_id = $guildId
        """.query[UserOutput].to[List].transact(xa)
    }

    def unbanUserFromGuild(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Int] = {
        sql"""
        DELETE FROM Guild_Ban
        WHERE (user_id = $userId) AND (guild_id = $guildId)
        """.update.run.transact(xa)
    }

    /////////////////////////// GUILD INVITES #1 ///////////////////////////

    // Version individuelle : même système que les demandes d'ami

    // on pourrait rajouter (qui a envoyé l'invitation) pour plus tard
    def sendGuildInvite(guildInviteInput: GuildInviteInput, xa: Transactor[IO]): IO[Int] = {
        val insertGuildInvite =
        sql"""
            INSERT INTO User_Guild (user_id, guild_id, invite_accepted)
            VALUES (${guildInviteInput.user_id}, ${guildInviteInput.guild_id}, 0)
        """.update.run
        insertGuildInvite.transact(xa)
    }

    def getGuildInvitesGuildnames(user_id: UUID, xa: Transactor[IO]): IO[List[(GuildInviteOutput)]] = {
        sql"""
            SELECT guild_id, guild_name FROM 
            User_Guild JOIN Guild ON User_Guild.guild_id = Guild.guild_id
            WHERE user_id = ${user_id.toString} AND invite_accepted = 0
        """
        .query[GuildInviteOutput]
        .to[List]
        .transact(xa)
    }

    def declineGuildInvite(user_id: UUID, guild_id:UUID, xa: Transactor[IO]): IO[Int] = {
        sql"""
            DELETE FROM User_Guild WHERE 
            user_id = ${user_id.toString} AND guild_id = ${guild_id.toString}
        """.update.run
        .transact(xa)
    }

    def acceptGuildInvite(guildInviteInput: GuildInviteInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            UPDATE User_Guild SET invite_accepted = 1 WHERE 
            user_id = ${guildInviteInput.user_id} AND guild_id = ${guildInviteInput.guild_id}
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
            
            // Création de guilde
            case req @ POST -> Root / "create" =>
                req.as[Json].flatMap { json =>
                    val guildName = json.hcursor.get[String]("guildName").getOrElse("")
                    val guildDescription = json.hcursor.get[String]("guildDescription").getOrElse("")
                    val ownerIdStr = json.hcursor.get[String]("ownerId").getOrElse("")

                    if (guildName.nonEmpty && ownerIdStr.nonEmpty) {
                        val ownerId = UUID.fromString(ownerIdStr)
                        createGuild(guildName, guildDescription, ownerId, xa).flatMap { guildId =>
                            Ok(Json.obj(
                                "message" -> Json.fromString("Serveur créé avec succès"),
                                "guildId" -> Json.fromString(guildId.toString)
                            ))
                        }
                    } else {
                        BadRequest(Json.obj("error" -> Json.fromString("Le nom du serveur et l'ID du propriétaire sont obligatoires.")))
                    }
                }
            
            // READ
            // Update : retourne un objet JSON de tous les attributs au lieu d'un tuple de l'id et du nom
            // ça facilite grandement l'accès aux données dans le front
            case GET -> Root / UUIDVar(id) =>
                getGuildById(id, xa).flatMap {
                    case Some((id, guildName, guildDesc, ownerId)) =>
                        Ok(Json.obj(
                            "guild_id" -> Json.fromString(id.toString),
                            "guild_name" -> Json.fromString(guildName),
                            "guild_desc" -> Json.fromString(guildDesc),
                            "owner_id" -> Json.fromString(ownerId.toString)
                        ))

                    case None =>
                        NotFound(Json.obj("error" -> Json.fromString("Guild not found")))
                }
            
            // Recup tous les Guild
            case GET -> Root =>
                getAllGuilds(xa).flatMap { guilds => 
                    Ok(guilds.asJson)
                }

            
            // Récup tous les users d'une guild
            case GET -> Root / UUIDVar(id) / "users" =>
                getUsersInGuild(id, xa).flatMap { users =>
                    Ok(users.asJson)
                }
            
            //// UPDATE ??


            //// DELETE

            // Version sans vérification de l'ownership
            // case DELETE -> Root / UUIDVar(id) =>
            //     getGuildById(id, xa).flatMap {
            //         guildOption =>
            //             guildOption match {
            //                 case Some(guild) => 
            //                     deleteGuild(id, xa).flatMap {
            //                         result =>
            //                             Ok(s"Users removed : $result")
            //                     }
            //                 case None => 
            //                     NotFound(s"Couldn't delete Guild with id $id : not found")
            //             }
            //     }
            
            // Suppression de guilde après vérification de l'ownership (WIP, ne reconnait pas encore l'owner de la guilde)
            case r @ DELETE -> Root / UUIDVar(guildId) =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        getGuildOwnerId(guildId, xa).flatMap {
                            case Some(ownerId) =>
                                if (ownerId.toString == userIdFromToken) {
                                    deleteGuild(guildId, xa).flatMap { result =>
                                        Ok(Json.obj("message" -> Json.fromString("Guild deleted"), "rows_deleted" -> Json.fromInt(result)))
                                    }
                                } else {
                                    Forbidden(Json.obj("error" -> Json.fromString("You are not the owner of this guild")))
                                }
                            case None =>
                                BadRequest(Json.obj("error" -> Json.fromString("Guild not found")))
                        }
                    case None =>
                        BadRequest(Json.obj("error" -> Json.fromString("Authorization header missing")))
                }
            
            // case req @ DELETE -> Root / UUIDVar(guildId) =>
            //     req.headers.get(ci"Authorization").map(_.head) match {
            //         case Some(authHeader) =>
            //             val token = authHeader.value.replace("Bearer ", "")
            //             getUserIdFromJWT(token) match {
            //                 case Some(requestingUserId) =>
            //                     getGuildById(guildId, xa).flatMap {
            //                         case Some((_, _, _, ownerId)) if ownerId == requestingUserId =>
            //                             deleteGuild(guildId, xa).flatMap { result =>
            //                                 Ok(Json.obj("message" -> Json.fromString("Guild deleted"), "rows_deleted" -> Json.fromInt(result)))
            //                             }
            //                         case Some(_) =>
            //                             Forbidden(Json.obj("error" -> Json.fromString("You are not the owner of this guild")))
            //                         case None =>
            //                             NotFound(Json.obj("error" -> Json.fromString("Guild not found")))
            //                     }
            //                 case None =>
            //                     // C'est pas le bon code de retour mais IMPOSSIBLE de faire fonctionner Unauthorized (skill issue)
            //                     Forbidden(Json.obj("error" -> Json.fromString("Invalid token")))
            //             }
            //         // Même problème ici
            //         case None =>
            //             Forbidden(Json.obj("error" -> Json.fromString("Authorization header missing")))
            //     }


            // kick un utilisateur
            case POST -> Root / UUIDVar(guildid) / "kick" / UUIDVar(userId) =>
                removeUserFromGuild(userId, guildid, xa).flatMap { result =>
                    Ok(s"Rows affected: $result")
                }

            
            ///////////////////////////// GESTION DES INVITATIONS ///////////////////

            // RAJOUTER DES TESTS POUR VOIR SI LA GUILD EXISTE
            // peut etre verifier que c'est un admin qui add ?
            case r @ POST -> Root / "invites" / "add" =>
                r.as[GuildInviteInput].attempt.flatMap {
                    case Right(guildInput) =>
                        if (guildInput.user_id.nonEmpty && guildInput.guild_id.nonEmpty) {
                            getGuildById(UUID.fromString(guildInput.guild_id), xa).flatMap {
                                case Some(_) =>
                                    sendGuildInvite(guildInput, xa).flatMap { result =>
                                        Ok(s"Rows affected: $result")
                                    }
                                case None =>
                                    BadRequest("Guild not found")
                            }
                        } else {
                            BadRequest("IDs must not be empty")
                        }
                    case Left(_) =>
                        BadRequest("Bad Request. Format { user_id: String, guild_id: String }")
                    }

            
            // pour afficher les requests "en attente" et de qui elles viennent
            case r @ GET -> Root / "invites" =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        r.as[Json].flatMap { json =>
                            val guild_id = json.hcursor.get[String]("guild_id").getOrElse("")
                            getGuildInvitesGuildnames(UUID.fromString(userIdFromToken), xa).flatMap { result =>
                                Ok(s"Rows affected: $result")
                            }
                        }

                    case None =>
                        BadRequest("Token not found")
                }


            // Refuse une friend request
            // j'hésite a en faire une route DELETE  
            // psk techniquement, ça fait supprimer un truc
            case r @ POST -> Root / "invites" / "decline" =>
                r.headers.get(ci"Authorization") match {
                    case Some(header) =>
                        val token = header.head.value.stripPrefix("Bearer ")

                        val userIdFromToken = Authentification.decodeToken(token)
                        r.as[Json].flatMap { json =>
                            val guild_id = json.hcursor.get[String]("guild_id").getOrElse("")
                            declineGuildInvite(UUID.fromString(userIdFromToken), UUID.fromString(guild_id), xa).flatMap { result =>
                                Ok(s"Rows affected: $result")
                            }
                        }

                    case None =>
                        BadRequest("Token not found")
                }


            case r @ POST -> Root / "ban" =>
                r.as[GuildInviteInput].attempt.flatMap {
                    case Right(input) =>
                        banUserFromGuild(UUID.fromString(input.user_id), UUID.fromString(input.guild_id), xa).flatMap { bannedId =>
                            Ok(s"Banned user ID : $bannedId")
                        }

                    case Left(_) =>
                        BadRequest("Bad request. Format { user_id: String, guild_id: String }")
                }

            case GET -> Root / UUIDVar(guildId) / "bans" =>
                getBannedUsers(guildId, xa).flatMap { banList =>
                    Ok(banList.asJson)
                }

            case GET -> Root / UUIDVar(guildId) / "ban" / UUIDVar(userId) =>
                checkIfUserIsBanned(userId, guildId, xa).flatMap { result =>
                    result match {
                        case 0 => // L'utilisateur n'a pas été banni : on peut lui envoyer une invitation
                            Ok(s"Permitted action")

                        case _ => // L'utilisateur a été banni du serveur : pas d'envoi d'invitation
                            Forbidden(s"Forbidden action : user has been banned from this guild")
                    }
                }

            case r @ DELETE -> Root / "unban" =>
                r.as[GuildInviteInput].attempt.flatMap {
                    case Right(input) =>
                        unbanUserFromGuild(UUID.fromString(input.user_id), UUID.fromString(input.guild_id), xa).flatMap { result =>
                            result match {
                                case 0 => // Pas d'utilisateur débanni donc on l'a pas trouvé
                                    NotFound("Guild or user not found")

                                case _ => // L'utilisateur a bien été débanni
                                    Ok(s"Rows affected : $result")
                            }
                        }

                    case Left(_) =>
                        BadRequest("Bad request. Format { user_id: String, guild_id: String")
                }
        }
    }
}

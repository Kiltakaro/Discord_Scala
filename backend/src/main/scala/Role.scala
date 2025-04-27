import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Json
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._
import org.typelevel.ci.CIStringSyntax

import org.http4s.ParseFailure
import cats.implicits._
import java.util.UUID
import doobie.util.meta.Meta

implicit val uuidMeta: Meta[UUID] = Meta[String].imap(UUID.fromString)(_.toString)

case class RoleInput(guildId: UUID, name: String, priority: Int)
case class PermissionInput(roleId: UUID, permission: String)
case class RoleUpdateInput(roleId: UUID, guildId: UUID, name: String, priority: Int)
case class RoleAssignment(userId: UUID, guildId: UUID, roleId: UUID)

object Role {

    def createRole(input: RoleInput, xa: Transactor[IO]): IO[Int] = {
        val newRoleId = UUID.randomUUID()
        sql"""
            INSERT INTO Role (role_id, guild_id, role_name, priority)
            VALUES ($newRoleId, ${input.guildId}, ${input.name}, ${input.priority})
        """.update.run.transact(xa)
    }

    def assignRole(assignment: RoleAssignment, xa: Transactor[IO]): IO[Int] = {
        sql"""
            INSERT INTO User_Role (user_id, guild_id, role_id)
            VALUES (${assignment.userId}, ${assignment.guildId}, ${assignment.roleId})
        """.update.run.transact(xa)
    }

    def assignPermission(input: PermissionInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            INSERT INTO Role_Permission (role_id, permission_name)
            VALUES (${input.roleId}, ${input.permission})
        """.update.run.transact(xa)
    }

    def removePermission(input: PermissionInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            DELETE FROM Role_Permission
            WHERE role_id = ${input.roleId} AND permission_name = ${input.permission}
        """.update.run.transact(xa)
    }

    def removeRole(assignment: RoleAssignment, xa: Transactor[IO]): IO[Int] = {
        sql"""
            DELETE FROM User_Role
            WHERE user_id = ${assignment.userId} AND guild_id = ${assignment.guildId} AND role_id = ${assignment.roleId}
        """.update.run.transact(xa)
    }

    def deleteRole(roleId: UUID, xa: Transactor[IO]): IO[Int] = {
        for {
            _ <- sql"DELETE FROM Role_Permission WHERE role_id = $roleId".update.run.transact(xa)
            _ <- sql"DELETE FROM User_Role WHERE role_id = $roleId".update.run.transact(xa)
            deleted <- sql"DELETE FROM Role WHERE role_id = $roleId".update.run.transact(xa)
        } yield deleted
    }

    def updateRole(input: RoleUpdateInput, xa: Transactor[IO]): IO[Int] = {
        sql"""
            ALTER TABLE Role
            UPDATE role_name = ${input.name}, priority = ${input.priority}
            WHERE role_id = ${input.roleId} AND guild_id = ${input.guildId}
        """.update.run.transact(xa)
    }

    def getUserPermissions(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[Set[String]] = {
        sql"""
            SELECT DISTINCT permission_name FROM Role_Permission
            JOIN User_Role ON Role_Permission.role_id = User_Role.role_id
            WHERE User_Role.user_id = $userId AND User_Role.guild_id = $guildId
        """.query[String].to[Set].transact(xa)
    }

    def getRolesInGuild(guildId: UUID, xa: Transactor[IO]): IO[List[(UUID, String, Int)]] = {
        sql"""
            SELECT role_id, role_name, priority FROM Role
            WHERE guild_id = $guildId
        """.query[(UUID, String, Int)].to[List].transact(xa)
    }

    def getRolesAssignedToUser(userId: UUID, guildId: UUID, xa: Transactor[IO]): IO[List[(UUID, String, Int)]] = {
        sql"""
            SELECT r.role_id, r.role_name, r.priority
            FROM Role r
            JOIN User_Role ur ON r.role_id = ur.role_id
            WHERE ur.user_id = $userId AND ur.guild_id = $guildId
        """.query[(UUID, String, Int)].to[List].transact(xa)
    }


    def roleRoutes(xa: Transactor[IO]) = HttpRoutes.of[IO] {

    case req @ GET -> Root / "permissions" / UUIDVar(guildId) =>
        req.headers.get(ci"Authorization") match {
            case Some(header) =>
                val token = header.head.value.stripPrefix("Bearer ")
                val userId = Authentification.decodeToken(token)
                getUserPermissions(UUID.fromString(userId), guildId, xa).flatMap { perms =>
                    Ok(perms.asJson)
                }
            case None =>
                BadRequest("Token not found")
        }

    case GET -> Root / UUIDVar(guildId) =>
        getRolesInGuild(guildId, xa).flatMap(roles => Ok(roles.asJson))

    case GET -> Root / "assigned" / UUIDVar(userId) / UUIDVar(guildId) =>
        getRolesAssignedToUser(userId, guildId, xa).flatMap(roles => Ok(roles.asJson))



    case req @ POST -> Root / "create" =>
      req.as[RoleInput].flatMap { input =>
        createRole(input, xa).flatMap(rows => Ok(Json.obj("created" -> Json.fromInt(rows))))
      }

    case req @ POST -> Root / "assign" =>
      req.as[RoleAssignment].flatMap { assignment =>
        assignRole(assignment, xa).flatMap(rows => Ok(Json.obj("assigned" -> Json.fromInt(rows))))
      }

    case req @ POST -> Root / "permissions" =>
      req.as[PermissionInput].flatMap { input =>
        assignPermission(input, xa).flatMap(rows => Ok(Json.obj("assigned" -> Json.fromInt(rows))))
      }

    case req @ DELETE -> Root / "permissions" =>
      req.as[PermissionInput].flatMap { input =>
        removePermission(input, xa).flatMap(rows => Ok(Json.obj("removed" -> Json.fromInt(rows))))
      }

    case req @ DELETE -> Root / "unassign" =>
      req.as[RoleAssignment].flatMap { assignment =>
        removeRole(assignment, xa).flatMap(rows => Ok(Json.obj("removed" -> Json.fromInt(rows))))
      }

    case req @ DELETE -> Root / UUIDVar(roleId) =>
      deleteRole(roleId, xa).flatMap(rows => Ok(Json.obj("deleted" -> Json.fromInt(rows))))

    case req @ PUT -> Root =>
      req.as[RoleUpdateInput].flatMap { input =>
        updateRole(input, xa).flatMap(rows => Ok(Json.obj("updated" -> Json.fromInt(rows))))
      }
    
  }
}

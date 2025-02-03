import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.typelevel.log4cats.LoggerFactory
import org.http4s.server.middleware.Logger

import org.typelevel.log4cats.slf4j.Slf4jFactory

import org.http4s.server.Router

import cats.data.NonEmptyList
import cats.implicits._
import java.util.UUID


// On peut exetends IOApp plutot que IOApp.Simple mais ça a l'air plus simple la version "IOApp.Simple"
// a voir plus tard
object Main extends IOApp {

    given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger = loggerFactory.getLogger

    // Routeur déplacé dans HttpServer.scala

    /////////////////// LANCEMENT /////////////////////////

    // https://http4s.org/v1/docs/client.html#setup
    // Lancement du serveur et de la connexion à la BDD simultanément
    def run(args: List[String]): IO[ExitCode] = {
        (Database.clickhouseTransactor, HttpServer.startServer[IO]).parTupled.use {
            case (xa, _) =>
                for {
                    _ <- Database.insertUser(xa) // Insert User
                    users <- Database.readUsers(xa) // fetch les users
                    _ <- IO(println(s"Utilisateurs en base : $users")) // Affichage, Normalement c'est la derniere ligne du terminal
                    _ <- IO.never // Au risque de me répéter, c'est pour éviter que le programme se termine (nous rende la main)
                } yield ExitCode.Success // ça c'est psk j'ai pas extend IOApp.Simple a voir plus tard
        }
    }
}

// import scala.concurrent.ExecutionContext
// import java.util.Properties
// import doobie.util.log.LogHandler

import doobie.hikari.HikariTransactor
import io.circe.generic.auto._
import io.circe.syntax._
import cats._
import cats.data._
import cats.implicits._

import cats.data.NonEmptyList
import cats.effect._
import doobie.implicits._
import doobie._

import java.util.UUID
// Very important to deal with arrays
import doobie.util.transactor.Transactor._


// Jvais essayer de me connecter a clickhouse ici pour pas flinguer le main
object Database {

    // APRES AVOIR REGARDER CE CODE JE VOUS CONSEILLE FORTEMENT DE LIRE LE TUTO MAIS APRES SINON
    // ça va etre comme moi : ça m'a pris 7 h pour faire ce truc :skull:

    // https://rockthejvm.com/articles/learning-doobie-for-the-greater-good

    // C'est bcp de copier coller
    // je mets la fonction du tuto puis la notre en dessous
    private val clickhouseUrl = "jdbc:clickhouse://localhost:8123/default"

    val clickhouseTransactor: Resource[IO, HikariTransactor[IO]] = for {
        ce <- ExecutionContexts.fixedThreadPool[IO](32)
        xa <- HikariTransactor.newHikariTransactor[IO](
            "com.clickhouse.jdbc.ClickHouseDriver", // le pilote pour clickhouse
            clickhouseUrl, // URL de connexion
            "default", // User
            "",    // password
            ce
        )
    } yield xa


    // Merci a ce gigachad
    // Sinon vous allez avoir des pb avec les UUID plus tard
    // https://stackoverflow.com/questions/64338748/how-do-i-convert-a-java-util-uuid-to-doobie-syntax-sqlinterpolator-singlefragmen
    implicit val uuidMeta: Meta[UUID] = Meta[String].imap[UUID](UUID.fromString)(_.toString)


    ///////////////// POUR INSERT //////////////////////

    // En gros un INSERT SQL stockée dans une variable
    // decommentez pour avoir les couleurs peut etre ça sera mieux

    // def saveActorProgram(name: String): IO[Int] = {
    //     val saveActor: doobie.ConnectionIO[Int] =
    //     sql"insert into actors (name) values ($name)".update.run
    //     saveActor.transact(xa)
    // }

    // Changement de la querry + de la chose a insert
    // PS ça m'a fait remarqué que tout le monde peut avoir le meme UUID y'a pas de sécurité pour les doublons
    def insertUser(xa: Transactor[IO]): IO[Int] = {
        // val userId = UUID.randomUUID().toString // toString() psk sinon ça bug voila pensez y.
        val userId2 = "88888888-8888-8888-8888-888888888888" // Fake UUID et Avec celui ci vous voyez VRAIMENT que ça marche
        val insertUser = // flemme de typer
        sql"""
            INSERT INTO User (user_id, username, password, creation_date)
            VALUES ($userId2, 'test2', 'password123', now())
        """.update.run
        insertUser.transact(xa)
    }

    ///////////////// POUR READ //////////////////////

    // def findAllActorsNamesProgram(xa: Transactor[IO]): IO[List[String]] = {
    //     val findAllActorsQuery: doobie.Query0[String] = sql"select username from User".query[String]
    //     val findAllActors: doobie.ConnectionIO[List[String]] = findAllActorsQuery.to[List]
    //     findAllActors.transact(xa)
    // }

    // Dans notre cas on veut récupérer les users
    // Changement de la querry SQL et du type de retour    
    def readUsers(xa: Transactor[IO]): IO[List[(UUID, String, String)]] = {
        val query = sql"SELECT user_id, username, password FROM User".query[(UUID, String, String)]
        val queryToList: doobie.ConnectionIO[List[(UUID, String, String)]] = query.to[List]
        queryToList.transact(xa)
        // jsuis quasi sur qu'on peut faire ça en une ligne genre 
        // query.to[List].transact(xa) sans les typages mais a voir plutot et c'est peut etre moins propre
    }
    // MTN allez regarder le fichier tips svp
}
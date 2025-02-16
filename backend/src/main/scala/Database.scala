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
}
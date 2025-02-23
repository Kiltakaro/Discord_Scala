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
import doobie.util.transactor.Transactor._


object Database {

    // https://rockthejvm.com/articles/learning-doobie-for-the-greater-good

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
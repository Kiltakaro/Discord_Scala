package db

import doobie._
import doobie.hikari._
import doobie.implicits._
import cats.effect._

object Database {
  val clickhouseUrl = "jdbc:clickhouse://localhost:8123/default"

  def transactor[F[_]: Async]: Resource[F, HikariTransactor[F]] = {
    HikariTransactor.newHikariTransactor[F](
      "com.clickhouse.jdbc.ClickHouseDriver",
      clickhouseUrl,
      "default",
      "" // No password needed for default user
    )
  }
}

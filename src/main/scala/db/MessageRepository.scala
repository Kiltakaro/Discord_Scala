package db

import doobie._
import doobie.implicits._
import cats.effect._
import models.Message

object MessageRepository {
  def createTable: ConnectionIO[Int] =
    sql"""
      CREATE TABLE IF NOT EXISTS messages (
        id String,
        content String,
        user_id String,
        channel_id String,
        timestamp DateTime DEFAULT now()
      ) ENGINE = MergeTree()
      ORDER BY timestamp
    """.update.run

  def insertMessage(message: Message): ConnectionIO[Int] =
    sql"""
      INSERT INTO messages (id, content, user_id, channel_id) VALUES
      (${message.id}, ${message.content}, ${message.userId}, ${message.channelId})
    """.update.run

  def getMessagesByChannel(channelId: String): ConnectionIO[List[Message]] =
    sql"""
      SELECT id, content, user_id, channel_id FROM messages
      WHERE channel_id = $channelId ORDER BY timestamp DESC LIMIT 50
    """.query[Message].to[List]
}

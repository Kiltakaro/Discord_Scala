import cats.effect._
import fs2.kafka._
import io.circe.parser._
import io.circe.generic.auto._
import doobie._
import doobie.implicits._
import java.util.UUID
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime



object MessageConsumer {
    val topic = "messages"
    
    val ip = "localhost:9092" // techniquement c'est ip + port mais jtrovue pas de nom stylé pour la var

    // https://fd4s.github.io/fs2-kafka/docs/quick-example
    val consumerSettings = ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers(ip)
        .withGroupId("group")


    def createMessage(content: String, senderId: UUID, channelId: UUID, sentAt: String, xa: Transactor[IO]): IO[Int] = {
        sql"""
            INSERT INTO Message (channel_id, sender_id, content, sent_at)
            VALUES (${channelId.toString}, ${senderId.toString}, $content, $sentAt)
        """.update.run.transact(xa)
    }

    // le format de date est différent pour clickhouse
    def changeDateFormat(sentAt: String): String = {
        val dateToFormat = ZonedDateTime.parse(sentAt)
        val expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        dateToFormat.format(expectedFormat)
    }

    def consumeMessage(message: String, xa: Transactor[IO]): IO[Unit] = {
        parse(message) match {
            case Right(json) =>
                val content = json.hcursor.get[String]("content").getOrElse("")
                val channelId = json.hcursor.get[String]("channel_id").getOrElse("")
                val senderId = json.hcursor.get[String]("sender_id").getOrElse("")
                
                val sent_at = json.hcursor.get[String]("sent_at").getOrElse("")
                val formattedSentAt = changeDateFormat(sent_at)

                if (content.nonEmpty && channelId.nonEmpty && senderId.nonEmpty && sent_at.nonEmpty) {
                    createMessage(content, UUID.fromString(senderId), UUID.fromString(channelId), formattedSentAt, xa).void // ignore le message de retour sinon ça change le format de retour de la fonction
                } else {
                    IO(println("Message mal formatté"))
                }
                
            case Left(error) =>
                IO(println(s"Erreur : $error"))
        }
    }

    // https://fd4s.github.io/fs2-kafka/api/fs2/kafka/consumer/KafkaConsume.html
    def runConsumer(xa: Transactor[IO]): IO[Unit] = {

        // exemple tiré d'un github mais jme souviens plus lequel
        // KafkaConsumer.resource(consumerSettings).use { consumer =>
        //   consumer.subscribeTo(topic)
        //   consumer.stream.compile.drain
        // }

        KafkaConsumer
            .stream(consumerSettings)
            .subscribeTo(topic)
            .records
            .evalMap(record => consumeMessage(record.record.value, xa)) 
            // dans le tuto y'avait consumeChunk sauf que j'ai pas compris comment traiter les chunks (des lots)
            // donc je fais un traitement un par un c'est plus simple
            .compile
            .drain
        
    }
}

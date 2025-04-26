import cats.effect._
import fs2.kafka._
import io.circe.generic.auto._
import io.circe.syntax._
import java.util.UUID

case class MessageInput(channel_id: UUID, sender_id: UUID, content: String, sent_at: String)

object MessageProducer {

    val topic = "messages"
    
    val ip = "localhost:9092" // techniquement c'est ip + port mais jtrovue pas nom stylé pour la var

    // https://fd4s.github.io/fs2-kafka/docs/quick-example
    val producerSettings = ProducerSettings[IO, String, String].withBootstrapServers(ip)
    
    def sendMessage(message: MessageInput): IO[Unit] = {

        val content = message.asJson.noSpaces
        val channel_id = message.channel_id.toString

        // Pour faire un message Kafka
        val record = ProducerRecord(topic, message.channel_id.toString, content) 
        val producerRecord = ProducerRecords.one(record)

        // pour le moment je fais pas de gestion d'erreur psk je sais pas trop comment la gerer
        KafkaProducer
            .resource(producerSettings)
            .use(_
                .produce(producerRecord)
                .flatten
                .void // ça permet de pas avoir de retour après avoir produce, interet ne pas changer le format de retour
                )
    }
}

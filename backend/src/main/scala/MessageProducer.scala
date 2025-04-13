import cats.effect._
import fs2.kafka._
import io.circe.generic.auto._
import io.circe.syntax._
import java.util.UUID


object MessageProducer {

    // pareil dans consumer
    val topic = "messages"  // pour le moment j'envoie tout dans le topic message mais faudra arranger ça jpense
    
    // à changer peut etre si on passe au cloud
    val ip = "localhost:9092" // techniquement c'est ip + port mais jtrovue pas nom stylé pour la var

    // https://fd4s.github.io/fs2-kafka/docs/quick-example
    val producerSettings = ProducerSettings[IO, String, String].withBootstrapServers(ip)
    
    def sendMessage(message: MessageOutputModel): IO[Unit] = {

        val content = message.asJson.noSpaces
        val channel_id = message.channel_id.toString

        // Pour faire un message Kafka
        // au niveau du des topics, on a pas encore discuter du nommage donc j'ai mis messages
        // pus tard : transformer topic en channel_id ou en sender_id ou garder messages ???
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

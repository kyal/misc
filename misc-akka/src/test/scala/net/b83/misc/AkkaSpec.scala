package net.b83.misc

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.specs2.mutable.SpecificationLike

import scala.concurrent.duration._
import scala.concurrent.Future



/**
 * Example spec for component.
 */
class AkkaSpec extends SpecificationLike with StrictLogging {

  val kafkaBootstrap = "localhost:9092"
  val groupId = "misc-akka-test"
  val topic = "fix-cleared-abn"

  def consumerSettings(implicit actorSystem: ActorSystem) =
    ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaBootstrap)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  "Example of Reactive Akka" >> {

    "Consumer.plainSource" >> {
      implicit val actorSystem = ActorSystem("Test")
      implicit val materializer = ActorMaterializer()
      import actorSystem.dispatcher

      val subscription = Subscriptions.assignmentWithOffset(new TopicPartition(topic, 0) -> 0L)

      Consumer.plainSource(consumerSettings, subscription)
        .mapAsync(1)(record =>
          Future(
            logger.info(record.value().substring(0, 80))
          ).map(_ => akka.Done)
        )
        .runWith(Sink.ignore)

      Thread.sleep(500)

      ok
    }


    "Consumer.committableSource" >> {
      implicit val actorSystem = ActorSystem("Test")
      implicit val materializer = ActorMaterializer()
      import actorSystem.dispatcher

      val subscription = Subscriptions.topics(topic)

      Consumer.committableSource(consumerSettings, subscription)
        .mapAsync(1)(msg =>
          Future(
            logger.info(msg.record.value().substring(0, 80))
          ).map(_ => msg.committableOffset)
        )
        .mapAsync(1)(_.commitScaladsl())
        .runWith(Sink.ignore)

      Thread.sleep(500)
      ok
    }


    "Consumer.committableSource batch" >> {
      implicit val actorSystem = ActorSystem("Test")
      implicit val materializer = ActorMaterializer()
      import actorSystem.dispatcher

      val subscription = Subscriptions.topics(topic)

      Consumer.committableSource(consumerSettings, subscription)
        .mapAsync(1)(msg =>
          Future(
            logger.info(msg.record.value().substring(0, 80))
          ).map(_ => msg.committableOffset)
        )
        .batch(max = 20, first => CommittableOffsetBatch.empty.updated(first))((batch, elem) =>
          batch.updated(elem)
        )
        .mapAsync(3)(_.commitScaladsl())
        .runWith(Sink.ignore)

      Thread.sleep(500)
      ok
    }


    "Consumer.committableSource groupedWithin" >> {
      implicit val actorSystem = ActorSystem("Test")
      implicit val materializer = ActorMaterializer()
      import actorSystem.dispatcher

      val subscription = Subscriptions.topics(topic)

      Consumer.committableSource(consumerSettings, subscription)
        .mapAsync(1)(msg =>
          Future(
            logger.info(msg.record.value().substring(0, 80))
          ).map(_ => msg.committableOffset)
        )
        .groupedWithin(10, 5.seconds)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty)((batch, elem) => batch.updated(elem)))
        .mapAsync(3)(_.commitScaladsl())
        .runWith(Sink.ignore)

      Thread.sleep(500)
      ok
    }


  }
}
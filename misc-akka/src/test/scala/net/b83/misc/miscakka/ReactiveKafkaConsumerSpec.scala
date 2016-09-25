package net.b83.misc.miscakka

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, KafkaConsumerActor, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.specs2.mutable.SpecificationLike

import scala.concurrent.duration._


/**
 * Emulates auto-magic configuration framework which takes cares
 * of serdes and connection endpoints.
 */
object ConsumerHelper {

  final val kafkaBootstrap = "localhost:9092"
  final val groupId = "misc-akka-test"
  final val topic = "AAA"
  final val altTopic = "BBB"


  def consumerSettings(implicit system: ActorSystem) =
    ConsumerSettings(
      system,
      new StringDeserializer,
      new StringDeserializer
    )
      .withBootstrapServers(kafkaBootstrap)
      .withGroupId(groupId)
      .withPollInterval(10.milliseconds)
      .withPollTimeout(10.milliseconds)


  def subscription = Subscriptions.topics(topic)
  def assignment(offset: Long = 0L, topic: String = topic) = Subscriptions.assignmentWithOffset(new TopicPartition(topic, 0) -> offset)
}


/**
 * Examples of using reactive-kafka integration.
 */
class ReactiveKafkaConsumerSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with StrictLogging {
  import ConsumerHelper._

  implicit val materializer = ActorMaterializer()
  import system.dispatcher


  "plainSource (for external storage of consumer offsets)" >> {

    "assignmentWithOffset should get messages from the beginning" >> {
      val probe = Consumer.plainSource(
        consumerSettings,
        assignment()
      )
        .map(_.offset())
        .runWith(TestSink.probe)

      probe
        .request(5)
        .expectNext(0, 1, 2, 3, 4)

      probe.cancel()
      ok
    }


    "subscribe with auto offset earliest should get messages from the beginning" >> {
      val probe = Consumer.plainSource(
        consumerSettings
          .withGroupId(ConsumerHelper.groupId + "-" + java.util.UUID.randomUUID().toString)
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
        subscription
      )
        .map(_.offset())
        .runWith(TestSink.probe)

      probe
        .request(5)
        .expectNext(0, 1, 2, 3, 4)

      probe.cancel()
      ok
    }


    "subscribe with auto offset latest should have no messages" >> {
      val probe = Consumer.plainSource(
        consumerSettings
          .withGroupId(ConsumerHelper.groupId + "-" + java.util.UUID.randomUUID().toString)
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"),
        subscription
      )
        .map(_.offset())
        .runWith(TestSink.probe)

      probe
        .request(1)
        .expectNoMsg()

      probe.cancel()
      ok
    }

  }


  "committableSource (for manually controlling consumer offset commits)" >> {

    "parallel commit after each message" >> {
      val probe = Consumer.committableSource(
        consumerSettings,
        assignment()
      )
        .mapAsync(4)(msg =>
          msg
            .committableOffset
            .commitScaladsl()
            .map(_ => msg.record.offset() -> msg.committableOffset.partitionOffset.offset)
        )
        .runWith(TestSink.probe)

      probe
        .request(5)
        .expectNext((0, 0), (1, 1), (2, 2), (3, 3), (4, 4))

      probe.cancel()
      ok
    }


    "batch commit" >> {
      val probe = Consumer.committableSource(
        consumerSettings,
        assignment()
      )
        .map(_.committableOffset)
        .batch(
          max = 5,
          first => CommittableOffsetBatch.empty.updated(first)
        ) { (batch, elem) =>
          batch.updated(elem)
        }
        .mapAsync(1)(msg => msg.commitScaladsl().map(_ => msg.offsets().values.max)) // Last offset per batch
        .runWith(TestSink.probe)

      probe
        .request(15)
        .expectNext(0, 5, 10) // Commits the first before we are batch back-pressured (only with parallelism of 1)

      probe.cancel()
      ok
    }


    "groupedWithin commit" >> {
      val probe = Consumer.committableSource(
        consumerSettings,
        assignment()
      )
        .map(_.committableOffset)
        .groupedWithin(5, 1.second)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty)((batch, elem) => batch.updated(elem)))
        .mapAsync(3)(msg => msg.commitScaladsl().map(_ => msg.offsets().values.max)) // Last offset per batch
        .runWith(TestSink.probe)

      probe
        .request(15)
        .expectNext(4, 9, 14)

      probe.cancel()
      ok
    }
  }


  "KafkaConsumerActor" >> {
    "shared by multiple Consumers" >> {

      val consumer = system.actorOf(KafkaConsumerActor.props(consumerSettings))

      val probe1 = Consumer.plainExternalSource[String, String](consumer, assignment())
        .map(_.offset())
        .runWith(TestSink.probe)

      val probe2 = Consumer.plainExternalSource[String, String](consumer, assignment(0L, altTopic))
        .map(_.offset())
        .runWith(TestSink.probe)

      probe1.request(5)
      probe2.request(5)

      probe1.expectNext(0, 1, 2, 3, 4)
      probe2.expectNext(0, 1, 2, 3, 4)


      probe1.cancel()
      probe2.cancel()
      system.stop(consumer)
      ok
    }

  }

}
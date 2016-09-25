package net.b83.misc.miscakka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka._
import akka.stream.{ActorMaterializer, KillSwitches}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.specs2.mutable.SpecificationLike

import scala.concurrent.Await
import scala.concurrent.duration._


/**
 * Emulates auto-magic configuration framework which takes cares
 * of serdes and connection endpoints.
 */
object ProducerHelper {

  final val kafkaBootstrap = "localhost:9092"
  final val topic = "AAA"


  def producerSettings(implicit system: ActorSystem): ProducerSettings[String, String] =
    ProducerSettings(
      system,
      new StringSerializer,
      new StringSerializer
    )
      .withBootstrapServers(kafkaBootstrap)


  def producerRecord(value: String) = new ProducerRecord[String, String](topic, value)
}


/**
 * Examples of using reactive-kafka integration.
 */
class ReactiveKafkaProducerSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with StrictLogging {
  import ProducerHelper._

  implicit val materializer = ActorMaterializer()

  "plainSink" >> {

    "basic publish" >> {

      val (probe, future) = TestSource.probe[String]
        .map(producerRecord)
        .toMat(Producer.plainSink(producerSettings))(Keep.both)
        .run()

      probe.expectRequest()
      probe.sendNext("AAA")
      probe.sendNext("BBB")
      probe.sendComplete()

      Await.result(future, 1.second) === Done
    }
  }


  "commitableSink" >> {
    "connect consumer to producer causing consumer commits to be driven by producer" >> {

      val (killSwitch, future) = Consumer.committableSource(
        ConsumerHelper.consumerSettings,
        ConsumerHelper.assignment()
      ).map(msg =>
        ProducerMessage.Message(
          producerRecord(msg.record.value()),
          msg.committableOffset
        )
      ).viaMat(KillSwitches.single)(Keep.right)
        .toMat(Producer.commitableSink(producerSettings))(Keep.both)
        .run()

      killSwitch.shutdown()
      Await.result(future, 1.second) === Done
    }


    "connect consumer and producer with commits and control materialized" >> {
      val (control, future) = Consumer.committableSource(
        ConsumerHelper.consumerSettings,
        ConsumerHelper.assignment()
      )
        .map(msg =>
          ProducerMessage.Message(
            producerRecord(msg.record.value()),
            msg.committableOffset
          )
        )
        .toMat(Producer.commitableSink(producerSettings))(Keep.both)
        .run()

      control.stop()
      Await.result(future, 1.second) === Done
    }

  }


  "as a Flow" >> {
    "from a Source" >> {

      val probe = Source(1 to 10)
        .map(n =>
          ProducerMessage.Message(
            producerRecord(n.toString),
            n // This is the pass through (not what was written to topic per-say)
          )
        )
        .via(Producer.flow(producerSettings))
        .map(result => result.message.passThrough -> result.offset)
        .runWith(TestSink.probe)

      probe
        .request(10)
        .expectNextN(10)
        .foreach(n => logger.info(s"passThrough -> offset=$n"))

      probe.cancel()
      ok
    }


    "passThrough from a commitableSource with batching" >> {
      val probe = Consumer.committableSource(
        ConsumerHelper.consumerSettings,
        ConsumerHelper.assignment()
      ).map(msg =>
        ProducerMessage.Message(
          producerRecord(msg.record.value()),
          msg.committableOffset
        )
      )
        .via(Producer.flow(producerSettings))
        .map(_.message.passThrough)
        .batch(
          max = 10,
          first => CommittableOffsetBatch.empty.updated(first)
        ) { (batch, elem) =>
          batch.updated(elem)
        }
        .mapAsync(2)(_.commitScaladsl())
        .runWith(TestSink.probe)

      probe
        .request(10)
        .expectNextN(10)

      probe.cancel()
      ok
    }
  }

}
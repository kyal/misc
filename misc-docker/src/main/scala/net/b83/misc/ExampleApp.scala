package net.b83.misc
import java.net.InetAddress
import java.nio.file.Path

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import configs.{Config, Configs, Result, ToConfig}
import configs.syntax._

import scala.concurrent.duration.FiniteDuration



object ExampleApp extends StrictLogging {

  final val ConfigRoot = "misc"

  // ADT is neat and powerful
  sealed trait AdtBase
  case class ItemA(xxx: String) extends AdtBase
  case class ItemB(yyy: Int) extends AdtBase


  // Custom config readers by providing implementations of Configs
  // Configs.from() will wrap extraction in a Try
  // or Configs.fromTry(cfg, path => A): Result[A]
  // Configs.map(...)
  // FromString.fromTry(parseFn, _.toString) for adding new string types
  case class MyConfig(`example-value`: BigDecimal,
                      opt: Option[String],
                      intervals: List[FiniteDuration],
                      path: Path,
                      enum: MyEnum,
                      ip: InetAddress,
                     `java-properties`: java.util.Properties,
                      adt: AdtBase)


  case class SimpleConfig(name: String)
  val DefaultSimpleConfig = SimpleConfig("default")


  def main(args: Array[String]): Unit = {

    val cfg = {
      val c = ConfigFactory.load()
      c.checkValid(ConfigFactory.defaultReference(), ConfigRoot)
      c.getConfig(ConfigRoot)
      val higherPriority = ConfigFactory.empty()
      c.getConfig(ConfigRoot) ++ higherPriority
    }



    logger.info("result with origin=" + cfg.getWithOrigin[Boolean]("avro.use-unions"))




    val config = cfg.get[MyConfig]("example-app")
    logger.info(s"using get=$config")


    val config2 = cfg.getConfig("example-app").extract[MyConfig]
    logger.info(s"using extract=$config2")

    val invalid = cfg.get[SimpleConfig]("invalid")
    logger.info("with default=" + invalid.valueOrElse(DefaultSimpleConfig))
    try {
      invalid.valueOrThrow(e => new IllegalArgumentException(e.toString))
    } catch { case e: Throwable => logger.info(s"valueOrThrow=$e") }


    // Short circuited on first error
    val combined = for {
      a <- cfg.get[SimpleConfig]("valid-one")
      b <- cfg.get[SimpleConfig]("valid-two")
    } yield a -> b
    logger.info("combined=" + combined)


    // Applicative
    val combinedBad = (cfg.get[SimpleConfig]("invalid") ~ cfg.get[SimpleConfig]("invalid"))(Tuple2.apply)
    logger.info("combinedBad] #errors=" + combinedBad.fold(_.entries.length, _ => 0))


    // Can create a Config from case objects and then render it like a source file
    val created = Config(
      "xxx" := 99,
      "yyy" := SimpleConfig("qqq")
    )
    logger.info("created=" + created.root().render())


  }

}


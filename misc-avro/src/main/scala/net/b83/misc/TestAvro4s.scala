package net.b83.misc
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s._



final case class Inner(bd: BigDecimal, pct: Option[Double] = Some(0.99))

case class MyValueClass(self: String) extends AnyVal


@AvroDoc("Example Avro doc string")
final case class MyCaseClass(name: String,
                             @AvroDoc("Example field doc") counter: Int,
                             inner: Inner,
                             either: Either[String, Long],
                             vc: MyValueClass)


object TestAvro4s {

  def main(args: Array[String]): Unit = {

    val obj = MyCaseClass(
      "XXX",
      68,
      Inner(
        BigDecimal("50.01")
      ),
      Right(99),
      MyValueClass("value class")
    )


    println("Schema for case class")
    val schema = AvroSchema[MyCaseClass]
    println(schema.toString(true))


    {
      println("Serializing to binary...")
      val os = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[MyCaseClass](os)
      output.write(obj)
      output.close()
      val bytes = os.toByteArray
      println("  #bytes=" + bytes.length)

      println("Deserializing from binary...")
      val in = new ByteArrayInputStream(bytes)
      val input = AvroInputStream.binary[MyCaseClass](in)
      val result = input.iterator.toSeq
      println("result=" + result.toString())
    }


    {
      println("Serializing to JSON")
      val os = new ByteArrayOutputStream()
      val output = AvroOutputStream.json[MyCaseClass](os)
      output.write(obj)
      output.close()
      println(os.toString("UTF-8"))
    }

  }
}

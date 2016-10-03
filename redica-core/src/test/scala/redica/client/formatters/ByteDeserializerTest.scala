package redica.client.formatters

import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks
import redica.util.ByteUtil

class ByteDeserializerTest extends FunSpec with PropertyChecks {

  describe("DefaultByteDeserializer") {
    it("should byteDeserializer") {
      forAll { original: Array[Byte] =>
        val actual = ByteDeserializer.byteDeserializer.fromBytesOpt(original)
        assert(actual === Some(original))
      }
    }

    it("should stringDeserializer") {
      forAll { original: String =>
        val actual = ByteDeserializer.stringDeserializer.fromBytesOpt(ByteUtil.getBytes(original))
        assert(actual === Some(original))
      }

      val original = "test\r\ntest\rtest\n"
      val actual = ByteDeserializer.stringDeserializer.fromBytesOpt(ByteUtil.getBytes(original))
      assert(actual === Some(original))
    }

    it("should intDeserializer") {
      forAll { original: Int =>
        val actual = ByteDeserializer.intDeserializer.fromBytesOpt(ByteUtil.getBytes(original.toString))
        assert(actual === Some(original))
      }
    }
  }

  describe("ByteDeserializer") {
    def equal[A](x: ByteDeserializer[A], y: ByteDeserializer[A]) = {
      forAll { original: String =>
        assert(x.fromBytesOpt(ByteUtil.getBytes(original)) === y.fromBytesOpt(ByteUtil.getBytes(original)))
      }
    }

    it("map") {
      equal(ByteDeserializer.stringDeserializer.map(identity), ByteDeserializer.stringDeserializer)

      forAll { (f: String => Array[Byte], g: Array[Byte] => Int) =>
        equal(ByteDeserializer.stringDeserializer.map(f).map(g), ByteDeserializer.stringDeserializer.map(f andThen g))
      }
    }
  }
}

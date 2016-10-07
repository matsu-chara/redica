package redica.client.formatters

import org.scalacheck.Arbitrary
import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks
import redica.util.ByteUtil

class ByteSerializerTest extends FunSpec with PropertyChecks {

  describe("DefaultByteSerializer") {
    it("should byteSerializer") {
      forAll { original: Array[Byte] =>
        val actual = ByteSerializer.byteSerializer.toBytes(original)
        assert(actual === original)
      }
    }

    it("should stringSerializer") {
      forAll { original: String =>
        val actual = ByteSerializer.stringSerializer.toBytes(original)
        assert(actual === ByteUtil.getBytes(original))
      }
    }

    it("should intSerializer") {
      forAll { original: Int =>
        val actual = ByteSerializer.intSerializer.toBytes(original)
        assert(actual === ByteUtil.getBytes(original.toString))
      }
    }
  }

  describe("ByteSerializer") {
    def equal[A: Arbitrary](x: ByteSerializer[A], y: ByteSerializer[A]) = {
      forAll { original: A =>
        assert(x.toBytes(original) === y.toBytes(original))
      }
    }

    it("contraMap") {
      equal(ByteSerializer.stringSerializer.contramap[String](identity), ByteSerializer.stringSerializer)

      forAll { (f: Array[Byte] => String, g: Int => Array[Byte]) =>
        equal(ByteSerializer.stringSerializer.contramap(f).contramap(g), ByteSerializer.stringSerializer.contramap(g andThen f))
      }
    }
  }
}

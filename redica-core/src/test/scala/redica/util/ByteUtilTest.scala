package redica.util

import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks

class ByteUtilTest extends FunSpec with PropertyChecks {

  describe("ByteUtil") {
    it("getBytes/fromBytes") {
      forAll { original: String =>
        assert(ByteUtil.fromBytes(ByteUtil.getBytes(original)) === original)
      }
    }
  }

}

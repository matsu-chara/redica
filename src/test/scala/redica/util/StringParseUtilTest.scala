package redica.util

import org.scalatest.FunSpec

class StringParseUtilTest extends FunSpec {

  describe("StringParseUtil") {
    it("should parse") {
      assert(StringParseUtil.safeParseInt("1") === Right(1))
      assert(StringParseUtil.safeParseInt("not int").isLeft)
    }
  }

}

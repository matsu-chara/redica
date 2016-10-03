package redica.client.io.protocols.request

import org.scalatest.FunSpec
import redica.util.ByteUtil

class RequestParserTest extends FunSpec {

  describe("RequestParserTest") {

    it("should multiBulk") {
      val sut = new RequestParser {}

      val actual = sut.multiBulk("SET", Seq(ByteUtil.getBytes("test"), ByteUtil.getBytes("value")))
      val expected = ByteUtil.getBytes(Seq("*3", "$3", "SET", "$4", "test", "$5", "value").mkString("", "\r\n", "\r\n"))

      assert(actual === expected)
    }

  }
}

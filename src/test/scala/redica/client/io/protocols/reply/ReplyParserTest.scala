package redica.client.io.protocols.reply

import java.io.InputStream

import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.util.ByteUtil

class ReplyParserTest extends FunSpec with MockitoSugar {

  describe("ReplyParserTest") {

    it("should bulk") {
      // "$4\r\ntest\r\n"

      val mockReader = mock[RespReader]
      when(mockReader.readLine(any[InputStream])).thenReturn(ByteUtil.getBytes("$4"))
      when(mockReader.readBytes(any[InputStream], Matchers.eq(4))).thenReturn(ByteUtil.getBytes("test"))

      val sut = new ReplyParser {
        override val respReader = mockReader
      }

      sut.bulk(null) match {
        case Right(bs) => assert(ByteUtil.getBytes("test") === bs)
        case Left(e) => fail(e)
      }

      // to ignore last lines
      verify(mockReader, times(2)).readLine(any[InputStream])
    }

    it("should status") {
      // "+OK\r\n"

      val mockReader = mock[RespReader]
      when(mockReader.readLine(any[InputStream])).thenReturn(ByteUtil.getBytes("+OK"))

      val sut = new ReplyParser {
        override val respReader = mockReader
      }

      sut.status(null) match {
        case Right(status) => assert(status === true)
        case Left(e) => fail(e)
      }

      // no need to ignore last line
      verify(mockReader, times(1)).readLine(any[InputStream])
    }

  }
}

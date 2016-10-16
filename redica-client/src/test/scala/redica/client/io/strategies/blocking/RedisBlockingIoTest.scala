package redica.client.io.strategies.blocking

import java.io.{ByteArrayInputStream, OutputStream}
import java.net.Socket
import java.util.concurrent.TimeUnit

import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.util.ByteUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RedisBlockingIoTest extends FunSpec with MockitoSugar {

  describe("RedisBlockingIoTest") {

    it("should send") {
      val out = mock[OutputStream]
      val in = new ByteArrayInputStream(ByteUtil.getBytes("$1\r\na\r\n"))
      val socket = mock[Socket]
      when(socket.getInputStream).thenReturn(in)
      when(socket.getOutputStream).thenReturn(out)

      val sut = new RedisBlockingIo(socket)
      Await.result(sut.send(Array[Byte](1, 2, 3)), Duration(1, TimeUnit.SECONDS))
      verify(out, times(1)).write(any[Array[Byte]])
    }

  }
}

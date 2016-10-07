package redica.client

import java.io.InputStream

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.RedisIo
import redica.util.ByteUtil

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RedisClientTest extends FunSpec with MockitoSugar {

  describe("RedisClientTest") {
    import scala.concurrent.ExecutionContext.Implicits.global

    it("should get") {
      val mockIo = mock[RedisIo]
      when(mockIo.send(any[Array[Byte]])(any[InputStream => Either[redica.client.io.exceptions.RedicaProtocolException, Array[Byte]]]()))
        .thenReturn(Future.successful(ByteUtil.getBytes("data")))
      val sut = new RedisClient(mockIo)
      val actual = sut.get[String, String]("test")
      assert(Await.result(actual, Duration.Inf) === "data")
    }

    it("should set") {
      val mockIo = mock[RedisIo]
      when(mockIo.send(any[Array[Byte]])(any[InputStream => Either[redica.client.io.exceptions.RedicaProtocolException, Boolean]]()))
        .thenReturn(Future.successful(true))
      val sut = new RedisClient(mockIo)
      val actual = sut.set("test", "data")

      assert(Await.result(actual, Duration.Inf) === true)
    }
  }
}

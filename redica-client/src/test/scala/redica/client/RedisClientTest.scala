package redica.client

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.RedisIo
import redica.util.ByteUtil

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RedisClientTest extends FunSpec with MockitoSugar {

  describe("RedisClientTest") {
    import scala.concurrent.ExecutionContext.Implicits.global

    it("should get") {
      val mockIo = mock[RedisIo]
      when(mockIo.send(any[Array[Byte]]))
        .thenReturn(Future.successful(ByteUtil.getBytes("data")))
      val sut = new RedisClient(mockIo)
      val actual = sut.get[String, String]("test")
      assert(Await.result(actual, Duration.Inf) === "data")
    }

    it("should set") {
      val mockIo = mock[RedisIo]
      when(mockIo.send(any[Array[Byte]])).thenReturn(Future.successful(Array(1.toByte)))
      val sut = new RedisClient(mockIo)
      val actual = sut.set("test", "data")

      assert(Await.result(actual, Duration.Inf) === true)
    }
  }
}

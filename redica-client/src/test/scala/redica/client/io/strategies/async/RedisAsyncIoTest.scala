package redica.client.io.strategies.async

import java.nio.channels.AsynchronousSocketChannel

import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify}
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.async.system.RootSystem
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest

class RedisAsyncIoTest extends FunSpec with MockitoSugar {

  describe("RedisNonRedisAsyncIoTest") {
    it("should send") {
      val system = mock[RootSystem]
      val sut = new RedisAsyncIo(Seq()) {
        override val rootSystem = system
      }
      try {
        sut.send(Array[Byte](1, 2, 3))
      } finally {
        sut.close()
      }

      verify(system, times(1)).receive(any[WriteRequest])
    }

    it("should close") {
      val chan = mock[AsynchronousSocketChannel]
      val sut = new RedisAsyncIo(Seq(chan))
      sut.close()
      verify(chan, times(1)).close()
    }
  }
}

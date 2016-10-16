package redica.client.io.strategies.async.system.worker.workers

import java.nio.channels.AsynchronousSocketChannel

import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.async.system.RootScheduler
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncConsumer.ReadRequest

import scala.concurrent.Promise

class RedisAsyncConsumerTest extends FunSpec with MockitoSugar {

  describe("RedisAsyncConsumer") {
    it("receive only once until casted") {
      val channel = mock[AsynchronousSocketChannel]
      val scheduler = mock[RootScheduler]

      val sut = new RedisAsyncConsumer(channel, scheduler)
      val promise = Promise[Array[Byte]]()

      sut.receive(ReadRequest(promise))
      verify(scheduler, times(1)).schedule(sut)

      sut.cast()
    }
  }
}

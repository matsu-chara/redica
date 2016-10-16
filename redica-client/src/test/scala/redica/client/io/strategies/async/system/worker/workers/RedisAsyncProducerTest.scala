package redica.client.io.strategies.async.system.worker.workers

import java.nio.channels.AsynchronousSocketChannel

import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.async.system.RootScheduler
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest

import scala.concurrent.Promise

class RedisAsyncProducerTest extends FunSpec with MockitoSugar {

  describe("RedisAsyncProducer") {
    it("receive only once until casted") {
      val channel = mock[AsynchronousSocketChannel]
      val consumer = mock[RedisAsyncConsumer]
      val scheduler = mock[RootScheduler]

      val sut = new RedisAsyncProducer(channel, consumer, scheduler)
      val promise = Promise[Array[Byte]]()

      sut.receive(WriteRequest(new Array[Byte](1), promise))
    }
  }
}

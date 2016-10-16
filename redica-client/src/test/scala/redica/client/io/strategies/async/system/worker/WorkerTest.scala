package redica.client.io.strategies.async.system.worker

import org.scalatest.FunSpec
import redica.client.io.strategies.async.system.RootScheduler
import redica.client.io.strategies.async.system.worker.exceptions.RedicaNotHandledException
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest
import redica.testutils.WriteRequestUtil

import scala.concurrent.Promise

class WorkerTest extends FunSpec {

  describe("WorkerTest") {

    it("should partialDoJob") {
      val sut = new Worker {
        override def cast(): Unit = ()

        override protected val rootScheduler: RootScheduler =  null
        override protected val partialReceive: Receive = {
          case _ => ()
        }
      }

      sut.receive(WriteRequestUtil.empty)
    }

    it("should throw exception when not handled") {
      val sut = new Worker {
        override def cast(): Unit = ()

        override protected val rootScheduler: RootScheduler = null
        override protected val partialReceive: Receive = PartialFunction.empty
      }

      assertThrows[RedicaNotHandledException](sut.receive(WriteRequest(Array.emptyByteArray, Promise[Array[Byte]]())))
    }

  }
}

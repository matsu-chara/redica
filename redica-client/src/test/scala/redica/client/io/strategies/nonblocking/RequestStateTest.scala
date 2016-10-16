package redica.client.io.strategies.nonblocking

import org.scalatest.FunSpec
import redica.client.io.protocols.reply.ReplyInProgress

class RequestStateTest extends FunSpec {

  describe("RequestState") {
   it("should update") {
     val state = new RequestState(Array(1,2,3).map(_.toByte))
     state.replyResult match {
       case ReplyInProgress(data) => assert(data.isEmpty)
       case _ => fail()
     }

     state.update(Array(4,5,6).map(_.toByte))
     state.replyResult match {
       case ReplyInProgress(data) => assert(data.deep === Array(4,5,6).map(_.toByte).deep)
       case _ => fail()
     }
   }
  }
}

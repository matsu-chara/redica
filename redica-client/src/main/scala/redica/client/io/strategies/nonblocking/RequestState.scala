package redica.client.io.strategies.nonblocking

import redica.client.io.protocols.reply.{ReplyInProgress, ReplyResult}

import scala.concurrent.Promise

class RequestState(val sendData: Array[Byte]) {
  val requestPromise = Promise[Array[Byte]]()

  var replyResult: ReplyResult[Array[Byte]] = ReplyInProgress(Array.emptyByteArray)

  def update(inProgressBytes: Array[Byte]): Unit = {
    replyResult = ReplyInProgress(inProgressBytes)
  }
}

package redica.client.io.protocols.reply

sealed trait ReplyResult[+A] extends Product with Serializable {
  def map[B](f: A => B): ReplyResult[B] = flatMap(a => ReplySuccess(f(a)))

  def inProgressMap(f: Array[Byte] => Array[Byte]): ReplyResult[A] = this match {
    case ReplyInProgress(p) => ReplyInProgress(f(p))
    case a => a
  }

  def inProgressForeach(f: Array[Byte] => Unit): Unit = this match {
    case ReplyInProgress(p) => f(p)
    case _ => ()
  }

  def flatMap[B](f: A => ReplyResult[B]): ReplyResult[B] = this match {
    case ReplySuccess(a) => f(a)
    case p: ReplyInProgress => p
    case e: ReplyFailed => e
  }
}

case class ReplySuccess[A](data: A) extends ReplyResult[A]
case class ReplyInProgress(partialData: Array[Byte]) extends ReplyResult[Nothing]
case class ReplyFailed(e: Throwable) extends ReplyResult[Nothing]

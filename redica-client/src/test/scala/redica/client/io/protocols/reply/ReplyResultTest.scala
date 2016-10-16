package redica.client.io.protocols.reply

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks
import redica.exceptions.RedicaException

import scala.collection.mutable

class ReplyResultTest extends FunSpec with PropertyChecks {

  implicit def arbReplyResult[A](implicit arb: Arbitrary[A]) = Arbitrary {
    val successGen = for {
      value <- arb.arbitrary
    } yield ReplySuccess(value)

    val inProgressGen = for {
      value <- implicitly[Arbitrary[Array[Byte]]].arbitrary
    } yield ReplyInProgress(value)

    val failedGen = Gen.const(ReplyFailed(new RedicaException("test")))

    Gen.frequency((6, successGen), (3, inProgressGen), (1, failedGen))
  }

  describe("ReplyResult") {
    def equal[A](x: ReplyResult[A], y: ReplyResult[A]): Boolean = (x, y) match {
      case (ReplyInProgress(a), ReplyInProgress(b)) => a.deep == b.deep
      case _ => x == y
    }

    it("should map") {
      forAll { replyResult: ReplyResult[Int] =>
        assert(equal(replyResult.map(identity), replyResult))

        forAll { (f: Int => String, g: String => Seq[Int]) =>
          assert(equal(replyResult.map(f).map(g), replyResult.map(f andThen g)))
        }
      }
    }

    it("should flatMap") {
      forAll { (i: Int, f: Int => ReplyResult[Int]) =>
        assert(equal(ReplySuccess(i).flatMap(f), f(i)))
      }

      forAll { replyResult: ReplyResult[Int] =>
        assert(equal(replyResult.flatMap(ReplySuccess.apply), replyResult))

        forAll { (f: Int => ReplyResult[String], g: String => ReplyResult[Seq[Int]]) =>
          assert(equal(replyResult.flatMap(f).flatMap(g), replyResult.flatMap { a => f(a).flatMap(g) }))
        }
      }
    }

    it("shuld inProgressMap") {
      forAll { replyResult: ReplyResult[Int] =>
        assert(replyResult.inProgressMap(identity) == replyResult)

        forAll { (f: Array[Byte] => Array[Byte], g: Array[Byte] => Array[Byte]) =>
          assert(equal(replyResult.inProgressMap(f).inProgressMap(g), replyResult.inProgressMap(f andThen g)))
        }
      }
    }

    it("should inProgressForeach") {
      val builder = mutable.ArrayBuilder.make[Byte]
      builder ++= Array(1,2,3).map(_.toByte)

      ReplyInProgress(Array(4,5,6).map(_.toByte)).inProgressForeach { x =>
        builder ++= x
      }
      assert(builder.result().deep === Array(1,2,3,4,5,6).map(_.toByte))
    }
  }
}

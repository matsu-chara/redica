package example

import java.util.concurrent.TimeUnit

import redica.client.RedisClientFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main {

  def main(args: Array[String]): Unit = {
    //    blocking()
    async()
  }

  def blocking(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val client = RedisClientFactory.connect("127.0.0.1", 6379)
    val result = for {
      _ <- client.set("a", "block")
      a <- client.getAsString("a")
      _ <- client.set("b", 3333)
      b <- client.getAsInt("b")
    } yield (a, b)

    println(Await.result(result, Duration.Inf)) // (block, 3333)
    client.close()
  }

  def async(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val client = RedisClientFactory.connectAsync("127.0.0.1", 6379)
    val result = Future.sequence(
      (1 to 1024*16).map { i =>
        Future(()) // empty Future for start many threads
          .flatMap { _ => client.set("a", i).zip(client.getAsInt("a")).zip(client.getAsString("b")).zip(client.set("b", "mofu"))
        }
      }
    ).flatMap { _ => client.getAsInt("a").zip(client.getAsString("b")) }

    println(Await.result(result, Duration(10, TimeUnit.SECONDS)))
    client.close()
  }
}


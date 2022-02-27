package org.zio.amazon.messing

import org.scanamo.generic.auto._
import org.scanamo.{ScanamoZio, Table}
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import zio.{Has, IO, UIO, ULayer, ZIO, ZLayer}

case class Dummy(key: String, bollix: String)
case class DbError(message: String, cause: Throwable)
trait DummyRepo {
  def add(key: String, bollix: String): IO[DbError, Unit]
}
case class DummyRepoLive() extends DummyRepo {
  val logger = LoggerFactory.getLogger(getClass.getName)

  val region = Region.EU_WEST_1
  val client = DynamoDbAsyncClient.builder()
    .region(region)
    .build()
  val scanamo = ScanamoZio(client)
  val table = Table[Dummy]("Dummy")
  override def add(key: String, bollix: String): IO[DbError, Unit] = {
    (for {
      _ <- UIO.effectTotal(logger.info(s"Persisting a dummy with key: $key and bollix: $bollix"))
      res <- scanamo.exec {
        for {
          res <- table.put(Dummy(key, bollix))
        } yield res
      }
      _ <- UIO.effectTotal(logger.info(s"Persisted a dummy with key: $key and bollix: $bollix."))
    } yield (res)).mapError(dynamoDbException => DbError(dynamoDbException.getMessage, dynamoDbException.getCause))
  }
}
object DummyRepoLive {
  val layer: ULayer[Has[DummyRepo]] = ZLayer.succeed(DummyRepoLive())
}
object DummyRepo {
  def add(key: String, bollix: String): ZIO[Has[DummyRepo], DbError, Unit] = {
    ZIO.serviceWith[DummyRepo](_.add(key, bollix))
  }
}

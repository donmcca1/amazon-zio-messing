package org.zio.amazon.messing

import org.scanamo.generic.auto._
import org.scanamo.{ScanamoZio, Table}
import org.scanamo.syntax._
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import zio.{Has, IO, UIO, ULayer, ZIO, ZLayer}

case class Dummy(key: String, bollix: String)
case class DbError(message: String, cause: Throwable)
trait DummyRepo {
  def add(key: String, bollix: String): IO[DbError, Unit]
  def get(key: String, bollixFinder: String): IO[DbError, Dummy]
  def getAll: IO[DbError, List[Dummy]]
}
object DummyRepo {
  def add(key: String, bollix: String): ZIO[Has[DummyRepo], DbError, Unit] = {
    ZIO.serviceWith[DummyRepo](_.add(key, bollix))
  }

  def get(key: String, bollixFinder: String): ZIO[Has[DummyRepo], DbError, Dummy] = {
    ZIO.serviceWith[DummyRepo](_.get(key, bollixFinder))
  }

  def getAll(): ZIO[Has[DummyRepo], DbError, List[Dummy]] = {
    ZIO.serviceWith[DummyRepo](_.getAll)
  }
}

class DummyRepoLive extends DummyRepo {
  import DummyRepoLive._

  override def add(key: String, bollix: String): IO[DbError, Unit] = {
    (for {
      _ <- UIO.effectTotal(logger.info(s"Persisting a dummy with key: $key and bollix: $bollix"))
      res <- scanamo.exec {
        for {
          res <- table.put(Dummy(key, bollix))
        } yield res
      }
      _ <- UIO.effectTotal(logger.info(s"Persisted a dummy with key: $key and bollix: $bollix."))
    } yield res).mapError(dynamoDbException => DbError(dynamoDbException.getMessage, dynamoDbException.getCause))
  }

  override def get(key: String, bollixFinder: String): IO[DbError, Dummy] = {
    for {
      _ <- UIO.effectTotal(logger.info(s"Retrieving dummy: $key, and finder: $bollixFinder from repository"))
      query <- scanamo.exec {
        for {
          res <- table.get("key" === key and ("bollix" === bollixFinder))
        } yield res
      }.mapError(dbEx => DbError(dbEx.getMessage, dbEx.getCause))
      either <- if (query.nonEmpty) UIO.succeed(query.get) else IO.fail(DbError(s"Could not find dummy: $key, and finder: $bollixFinder", new RuntimeException))
      result <- either match {
        case Right(dummy) => UIO.succeed(dummy)
        case Left(_) => IO.fail(DbError("dynamo read exception", new RuntimeException))
      }
      _ <- UIO.effectTotal(logger.info(s"Retrieved dummy from repo: $result"))
    } yield result
  }

  override def getAll: IO[DbError, List[Dummy]] = {
    (for {
      _ <- UIO.effectTotal(logger.info("Retrieving all the dummies in the repo"))
      res <- scanamo.exec {
        for {
          res <- table.scan()
        } yield res
      }
      list <- ZIO.effect(res.filter(_.isRight).map(_.getOrElse(throw new RuntimeException)))
      _ <- UIO.effectTotal(logger.info(s"Retrieved all the dummies from the repo: $list"))
    } yield list).mapError(_ => DbError("unable to retrieve dummies from the db...", new RuntimeException))
  }
}
object DummyRepoLive {
  val logger = LoggerFactory.getLogger(getClass.getName)

  val region = Region.EU_WEST_1
  val client = DynamoDbAsyncClient.builder()
    .region(region)
    .build()
  val scanamo = ScanamoZio(client)
  val table = Table[Dummy]("Dummy")

  val layer: ULayer[Has[DummyRepo]] = ZLayer.succeed(DummyRepoLive())

  def apply(): DummyRepoLive = new DummyRepoLive
}

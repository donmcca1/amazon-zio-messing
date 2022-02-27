package org.zio.amazon.messing

import org.slf4j.LoggerFactory
import org.zio.amazon.messing.DummyEventHandler.logger
import zio._
import io.circe._
import io.circe.parser.decode
import io.circe.generic.auto._

case class DummyEvent(body: String)
sealed trait DummyResponse
case class DummySuccess(message: String) extends DummyResponse
case class DummyFailure(message: String) extends DummyResponse
object DummyEventHandler {
  val logger = LoggerFactory.getLogger(getClass.getName)

  def apply(event: DummyEvent): DummyEventHandler = new DummyEventHandler(event)
}
class DummyEventHandler(event: DummyEvent) {
  val NUM_FIBRES = 5

  def handle(): DummyResponse = {
    zio.Runtime.default.unsafeRun(
      myApp.provideLayer(DummyRepoLive.layer)
    )
  }

  val myApp: ZIO[Has[DummyRepo], DummyFailure, DummyResponse] = (for {
    _ <- UIO.effectTotal(logger.info(s"Processing event: $event"))
    res <- UIO.effectTotal(decode[Record](event.body)).absolve
      .mapError(error => DummyFailure(s"Could not parse event $event. Exception: $error"))
      .flatMap(record => DummyRepo.add(record.key, record.value)
        .foldM(error => UIO.succeed(DummyFailure(s"Could not persist event $event. Exception: ${error}")),
          _ => UIO.succeed(DummySuccess("Event Persisted"))))
  } yield (res))

  /*val myApp = (for {
    _ <- UIO.effectTotal(logger.info(s"Processing event: $event"))
    res <- ZIO.foreachParN(NUM_FIBRES)(event.iterator)(entry => DummyRepo.persist(entry._1, entry._2).map(_ => entry)).optional
  } yield (res.map(_ => "200 OK").getOrElse("500 Internal Server Error")))*/
}

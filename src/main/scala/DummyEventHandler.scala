package org.zio.amazon.messing

import DummyEventHandler.logger

import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe._

import org.slf4j.LoggerFactory
import zio._
sealed trait DummyEvent
case object GetDummiesEvent extends DummyEvent
case class GetDummyEvent(dummy: String) extends DummyEvent
case class AddDummyEvent(body: String) extends DummyEvent
sealed trait DummyResponse
case class DummySuccess(message: String) extends DummyResponse
case class DummyFailure(message: String) extends DummyResponse
object DummyEventHandler {
  val logger = LoggerFactory.getLogger(getClass.getName)

  def apply(event: DummyEvent): DummyEventHandler = new DummyEventHandler(event)
}
class DummyEventHandler(event: DummyEvent) {
  def handle(): DummyResponse = {
    zio.Runtime.default.unsafeRun(
      myApp.provideLayer(DummyRepoLive.layer)
    )
  }

  val myApp: ZIO[Has[DummyRepo], DummyFailure, DummyResponse] = (for {
    _ <- UIO.effectTotal(logger.info(s"Processing event: $event"))
    dummyEvent <- UIO.succeed(event)
    res <- dummyEvent match {
        case add: AddDummyEvent => UIO.effectTotal(decode[Record](add.body)).absolve
          .mapError(error => DummyFailure(s"Could not parse event $event. Exception: $error"))
          .flatMap(record => DummyRepo.add(record.key, record.value)
            .foldM(error => UIO.succeed(DummyFailure(s"Could not persist event $event. Exception: ${error}")),
              _ => UIO.succeed(DummySuccess("Event Persisted"))))
        case get: GetDummyEvent => DummyRepo.get(get.dummy).foldM(error => UIO.succeed(DummyFailure(s"Could not retrieve dummy: ${get.dummy}, excpetion: $error")),
          success => UIO.succeed(DummySuccess(success.toString)))
        case _: GetDummiesEvent.type => DummyRepo.getAll().foldM(error => UIO.succeed(DummyFailure(s"Could not retrieve dummies., exception: $error")),
          success => UIO.succeed(DummySuccess(success.toString())))
      }
  } yield (res))
}

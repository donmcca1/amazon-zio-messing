package org.zio.amazon.messing

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import org.slf4j.LoggerFactory
import io.circe.syntax._
import io.circe._
import io.circe.generic.auto._

import scala.jdk.CollectionConverters._

class FirstLambdaInvocation extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  val logger = LoggerFactory.getLogger(getClass.getName)

  implicit val encoder: Encoder[Context] = (a: Context) => a.toString.asJson

  def handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    logger.info("ENVIRONMENT VARIABLES: " + System.getenv().asScala.asJson)
    logger.info("CONTEXT: " + context.asJson)
    val response = DummyEventHandler(DummyEvent(event.getBody)).handle()
    logger.info("EVENT: " + event.asJson)
    logger.info("EVENT TYPE: " + event.getClass.toString)
    val responseEvent = new APIGatewayProxyResponseEvent()
    responseEvent.setBody(response.asJson.toString())
    responseEvent.setStatusCode(response match {
      case _: DummySuccess => 201
      case _: DummyFailure => 500
    })
    responseEvent
  }
}

package parcalc

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class EvaluationRequest(expression: String)
case class EvaluationResponse(result: Int)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val evaluationRequestFormat = jsonFormat1(EvaluationRequest)
  implicit val evaluationResponseFormat = jsonFormat1(EvaluationResponse)
}
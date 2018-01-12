package parcalc

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalactic.TolerantNumerics
import org.scalatest.{Matchers, WordSpecLike}
import spray.json._

class RestTest extends WordSpecLike with Matchers with ScalatestRouteTest with RestService {

  implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.01)

  "REST API" should {
    "evaluate given expression on /evaluate endpoint" in {
      val (expr, result) = ("(1-1)*2+3*(1-3+4)+10/2", 11.0)

      checkResponse(expr) {
        status shouldEqual StatusCodes.OK
        responseAs[EvaluationResponse] shouldEqual EvaluationResponse(result)
      }
    }
    "respond with error code on parsing error" in {
      val expr = "1+(1"

      checkResponse(expr) {
        status shouldEqual StatusCodes.UnprocessableEntity
      }
    }
    "respond with error code on zero division" in {
      val expr = "1/0"

      checkResponse(expr) {
        status shouldEqual StatusCodes.UnprocessableEntity
      }
    }
    "respond with error code on (inf-inf) operation" in {
      val expr = "1/0-1/0"

      checkResponse(expr) {
        status shouldEqual StatusCodes.UnprocessableEntity
      }
    }
  }

  private def checkResponse[T](expr: String)(condition: => T) = {
    val request = createRequest(expr)

    request ~> route ~> check(condition)
  }

  private def createRequest(expr: String) = HttpRequest(
    HttpMethods.POST,
    uri = "/evaluate",
    entity = HttpEntity(
      MediaTypes.`application/json`,
      EvaluationRequest(expr).toJson.compactPrint
    )
  )

}
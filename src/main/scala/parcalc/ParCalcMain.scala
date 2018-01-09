package parcalc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn

object ParCalcMain extends App with Directives with JsonSupport {
  val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("ParCalcSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(120.seconds)

  val route =
    path("evaluate") {
      post {
        entity(as[EvaluationRequest]) { case EvaluationRequest(exprStr) =>
          val parsingActor = system.actorOf(ParsingActor.props)
          onSuccess(parsingActor ? exprStr) {
            case result: Double =>
              complete(EvaluationResponse(result))
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

  val appIP = config.getString("application.ip")
  val appPort = config.getInt("application.port")

  val bindingFuture = Http().bindAndHandle(route, appIP, appPort)

  println(s"Server online at http://$appIP:$appPort/")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}

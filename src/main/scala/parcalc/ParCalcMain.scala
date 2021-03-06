package parcalc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.io.StdIn

trait RestService extends Directives with JsonSupport {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout = Timeout(120.seconds)

  val route =
    path("evaluate") {
      post {
        entity(as[EvaluationRequest]) { case EvaluationRequest(exprStr) =>
          val parsingActor = system.actorOf(ParsingActor.props)
          onSuccess(parsingActor ? exprStr) {
            case Right(result: Double) =>
              complete(EvaluationResponse(result))
            case Left(_) =>
              complete(StatusCodes.UnprocessableEntity)
          }
        }
      }
    }
}

object ParCalcMain extends App with RestService {
  val config = ConfigFactory.load()
  val appIP = config.getString("application.ip")
  val appPort = config.getInt("application.port")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(route, appIP, appPort)

  println(s"Server online at http://$appIP:$appPort/")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}

package parcalc

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.duration._


object EvaluationActor {
  def props = Props(new EvaluationActor)
}

class EvaluationActor extends Actor {
  implicit val timeout = Timeout(120.seconds)
  implicit val executionContext = context.system.dispatcher

  override def receive: Receive = {
    case Num(x) =>
      sender ! x
      context stop self
    case BinOp(op, l, r) =>
      val left = (newWorker ? l).mapTo[Double]
      val right = (newWorker ? r).mapTo[Double]
      val result = left.zipWith(right) {
        op(_, _)
      }
      result pipeTo sender() onComplete { _ =>
        context stop self
      }
  }

  private def newWorker = context actorOf EvaluationActor.props
}

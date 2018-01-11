package parcalc

import akka.actor.{Actor, ActorRef, Props}

object EvaluationActor {
  def props = Props(new EvaluationActor)
}

class EvaluationActor extends Actor {
  private var operation: Operation = _

  private var left: Option[Double] = None
  private var right: Option[Double] = None

  private var actorL: ActorRef = _
  private var actorR: ActorRef = _

  override def receive: Receive = {
    case Num(x) =>
      sender ! x
      context stop self
    case BinOp(op, Num(l), Num(r)) =>
      sender ! op.apply(l, r)
      context stop self
    case BinOp(op, Num(l), r) =>
      operation = op
      left = Some(l)
      actorR = newWorker
      actorR ! r
    case BinOp(op, l, Num(r)) =>
      operation = op
      right = Some(r)
      actorL = newWorker
      actorL ! l
    case BinOp(op, l, r) =>
      operation = op
      actorL = newWorker
      actorR = newWorker
      actorL ! l
      actorR ! r
    case x: Double =>
      if (sender == actorL) left = Some(x)
      else right = Some(x)
      applyIfFinished()
  }

  private def applyIfFinished(): Unit =
    (left, right) match {
      case (Some(l), Some(r)) =>
        context.parent ! operation.apply(l, r)
        context stop self
      case _ =>
    }

  private def newWorker = context actorOf EvaluationActor.props
}

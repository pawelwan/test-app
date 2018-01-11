package parcalc

import akka.actor.{Actor, ActorRef, Props}

object ParsingActor {
  def props = Props(new ParsingActor)
}

class ParsingActor extends Actor {
  private var actor: ActorRef = _

  override def receive: Receive = {
    case exprStr: String =>
      actor = sender
      val parseResult = ArithmeticParser.parseExpr(exprStr)
      val result = parseResult map (Right(_)) getOrElse Left(ParsingError)
      result match {
        case Right(expr) =>
          val actor = context actorOf EvaluationActor.props
          actor ! expr
        case _ =>
          actor ! result
          context stop self
      }

    case result: Double =>
      val res =
        if (result.isInfinity || result.isNaN) Left(ZeroDivisionError)
        else Right(result)
      actor ! res
      context stop self
  }
}

sealed trait CalculationError
case object ParsingError extends CalculationError
case object ZeroDivisionError extends CalculationError

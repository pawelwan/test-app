package parcalc

import akka.actor.{Actor, Props}

object ParsingActor {
  def props = Props(new ParsingActor)
}

class ParsingActor extends Actor {
  override def receive: Receive = {
    case exprStr: String =>
      val parseResult = ArithmeticParser.parseExpr(exprStr)
      sender ! parseResult.map(x => {println(x); evaluate(x)}).get
      context stop self
  }

  def evaluate(expr: Expr): Double = expr match {
    case Num(x) => x
    case BinOp(op, l, r) => op.apply(evaluate(l), evaluate(r))
  }
}

package parcalc

import scala.util.parsing.combinator._

object ArithmeticParser extends JavaTokenParsers {
  def expr: Parser[Expr] =
    term ~! rep("+" ~! term | "-" ~! term) ^^ {
      case term ~ terms =>
        val adds = terms map {
          case "-" ~ t => BinOp(Sub, Num(0.0), t)
          case _ ~ t => t
        }
        makeBalancedAST(Add, term :: adds)
    }

  def term: Parser[Expr] =
    factor ~! rep("*" ~! factor | "/" ~! factor) ^^ {
      case factor ~ factors =>
        val muls = factors map {
          case "/" ~ f => BinOp(Div, Num(1.0), f)
          case _ ~ f => f
        }
        makeBalancedAST(Mul, factor :: muls)
    }

  def factor: Parser[Expr] = (
    "(" ~! expr ~! ")" ^^ { case _ ~ expr ~ _ => expr }
  | floatingPointNumber ^^ { x => Num(x.toDouble) }
  )

  def parseExpr(expression: String): ParseResult[Expr] = parseAll(expr, expression)

  private def makeBalancedAST(op: Operation, xs: List[Expr]): Expr = xs match {
    case List(x) => x
    case _ =>
      val half = xs.length / 2
      val (l, r) = xs splitAt half
      BinOp(op, makeBalancedAST(op, l), makeBalancedAST(op, r))
  }
}

sealed class Operation(val apply: (Double, Double) => Double)
case object Add extends Operation(_ + _)
case object Sub extends Operation(_ - _)
case object Mul extends Operation(_ * _)
case object Div extends Operation(_ / _)

sealed trait Expr
case class BinOp(op: Operation, l: Expr, r: Expr) extends Expr
case class Num(value: Double) extends Expr

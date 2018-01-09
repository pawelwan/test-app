package parcalc

import scala.util.parsing.combinator._

object ArithmeticParser extends JavaTokenParsers {
  def expr: Parser[Expr] =
    term ~! rep("+" ~! term | "-" ~! term) ^^ {
      case term ~ terms => terms.foldLeft(term) { (x, y) =>
        y match {
          case "+" ~ fact => BinOp(Add, x, fact)
          case "-" ~ fact => BinOp(Sub, x, fact)
        }
      }
    }

  def term: Parser[Expr] =
    factor ~! rep("*" ~! factor | "/" ~! factor) ^^ {
      case factor ~ factors => factors.foldLeft(factor) { (x, y) =>
        y match {
          case "*" ~ ter => BinOp(Mul, x, ter)
          case "/" ~ ter => BinOp(Div, x, ter)
        }
      }
    }

  def factor: Parser[Expr] = (
    "(" ~! expr ~! ")" ^^ { case _ ~ expr ~ _ => expr }
  | floatingPointNumber ^^ { x => Num(x.toDouble) }
  )

  def parseExpr(expression: String): ParseResult[Expr] = parseAll(expr, expression)
}

sealed class Operation(val apply: (Double, Double) => Double)
case object Add extends Operation(_ + _)
case object Sub extends Operation(_ - _)
case object Mul extends Operation(_ * _)
case object Div extends Operation(_ / _)

sealed trait Expr
case class BinOp(op: Operation, l: Expr, r: Expr) extends Expr
case class Num(value: Double) extends Expr

package parcalc

import org.scalatest.{Matchers, WordSpecLike}

class ArithmeticParserTest extends WordSpecLike with Matchers {

  val baseExprs = List(
    ("3", Num(3.0)),
    ("3.0", Num(3.0)),
    ("3+4", BinOp(Add, Num(3.0), Num(4.0))),
    ("3*4", BinOp(Mul, Num(3.0), Num(4.0)))
  )

  val priorityExprs = List(
    ("2+3*4", BinOp(Add, Num(2.0), BinOp(Mul, Num(3.0), Num(4.0)))),
    ("2*3+4", BinOp(Add, BinOp(Mul, Num(2.0), Num(3.0)), Num(4.0))),
    ("(2*3)+4", BinOp(Add, BinOp(Mul, Num(2.0), Num(3.0)), Num(4.0))),
    ("2*(3+4)", BinOp(Mul, Num(2.0), BinOp(Add, Num(3.0), Num(4.0))))
  )

  val subDivExprs = List(
    ("3-4", BinOp(Add, Num(3.0), BinOp(Sub, Num(0.0), Num(4.0)))),
    ("3/4", BinOp(Mul, Num(3.0), BinOp(Div, Num(1.0), Num(4.0))))
  )

  val optimizeExprs = List(
    (
      "1+2+3+4",
      BinOp(Add,
        BinOp(Add, Num(1.0), Num(2.0)),
        BinOp(Add, Num(3.0), Num(4.0))
      )
    ),
    (
      "1+2-3+4",
      BinOp(Add,
        BinOp(Add, Num(1.0), Num(2.0)),
        BinOp(Add,
          BinOp(Sub, Num(0.0), Num(3.0)),
          Num(4.0)
        )
      )
    ),
    (
      "1*2*3*4",
      BinOp(Mul,
        BinOp(Mul, Num(1.0), Num(2.0)),
        BinOp(Mul, Num(3.0), Num(4.0))
      )
    ),
    (
      "1*2/3*4",
      BinOp(Mul,
        BinOp(Mul, Num(1.0), Num(2.0)),
        BinOp(Mul,
          BinOp(Div, Num(1.0), Num(3.0)),
          Num(4.0)
        )
      )
    ),
    (
      "1+2+3*4*5*6+7",
      BinOp(Add,
        BinOp(Add,Num(1.0),Num(2.0)),
        BinOp(Add,
          BinOp(Mul,
            BinOp(Mul,Num(3.0),Num(4.0)),
            BinOp(Mul,Num(5.0),Num(6.0))
          ),
          Num(7.0)
        )
      )
    )
  )

  val malformedExprs = List(
    "(1",
    "1)",
    "1-2+",
    "1++",
    "1+(2+3"
  )

  "ArithmeticParser" should {
    baseExprs foreach { case (expr, res) =>
      s"parse basic expression: $expr" in {
        checkResult(expr, res)
      }
    }

    priorityExprs foreach { case (expr, res) =>
      s"keep operation priority: $expr" in {
        checkResult(expr, res)
      }
    }

    subDivExprs foreach { case (expr, res) =>
      s"change subtraction to addition (division to multiplication): $expr" in {
        checkResult(expr, res)
      }
    }

    optimizeExprs foreach { case (expr, res) =>
      s"optimize expressions to create balanced AST: $expr" in {
        checkResult(expr, res)
      }
    }

    malformedExprs foreach { expr =>
      s"return error on malformed expression: $expr" in {
        val parseResult = ArithmeticParser.parseExpr(expr)
        assert(!parseResult.successful)
      }
    }
  }

  def checkResult(expr: String, res: Expr): Unit = {
    val parseResult = ArithmeticParser.parseExpr(expr)

    assert(parseResult.successful)
    assert(parseResult.get === res)
  }
}

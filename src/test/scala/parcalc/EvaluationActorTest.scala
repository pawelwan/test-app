package parcalc

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalactic.TolerantNumerics
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class EvaluationActorTest extends TestKit(ActorSystem())
  with WordSpecLike with BeforeAndAfterAll {

  implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.01)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val ops = List(
    (Num(3.0), 3.0),
    (BinOp(Add, Num(3.0), Num(4.0)), 7.0),
    (BinOp(Sub, Num(3.0), Num(4.0)), -1.0),
    (BinOp(Mul, Num(3.0), Num(4.0)), 12.0),
    (BinOp(Div, Num(3.0), Num(4.0)), 0.75),
    (BinOp(Add, BinOp(Add, Num(2.0), Num(2.0)), Num(1.0)), 5.0),
    (BinOp(Sub, BinOp(Div, Num(2.0), Num(2.0)), Num(1.0)), 0.0),
    (BinOp(Mul, BinOp(Mul, Num(2.0), Num(3.0)), Num(4.0)), 24.0),
    (BinOp(Div, BinOp(Mul, Num(2.0), Num(3.0)), Num(4.0)), 1.5),
    (BinOp(Add, BinOp(Div, Num(1.0), Num(2.0)), BinOp(Mul, Num(3.0), Num(4.0))), 12.5)
  )

  "EvaluationActor" should {
    ops foreach { case (expr, res) =>
      s"perform operation $expr" in {
        basicTest(expr, _ === res)
      }
    }

    "return infinity on zero division" in {
      val expr = BinOp(Div, Num(1.0), Num(0.0))
      basicTest(expr, _.isInfinity)
    }

    "return NaN on (inf-inf) operation" in {
      val expr = BinOp(Sub, BinOp(Div, Num(1.0), Num(0.0)), BinOp(Div, Num(1.0), Num(0.0)))
      basicTest(expr, _.isNaN)
    }
  }

  def basicTest(expr: Expr, condition: Double => Boolean): Unit = {
    val parent = TestProbe()
    val child = parent.childActorOf(EvaluationActor.props)

    parent.send(child, expr)

    parent.expectMsgPF() {
      case x: Double =>
        assert(condition(x))
    }
  }
}

package parcalc

import akka.actor.{Actor, Props}

object BasicActor {
  def props = Props(new BasicActor)
}

class BasicActor extends Actor {
  override def receive: Receive = {
    case exprStr: String =>
      sender ! 11
  }
}
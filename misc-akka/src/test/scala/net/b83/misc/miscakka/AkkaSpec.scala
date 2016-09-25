package net.b83.misc.miscakka

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.StrictLogging
import org.specs2.mutable.SpecificationLike


class StupidActor extends Actor {
  def receive = {
    case msg => sender ! msg
  }
}


/**
 * Misc Akka examples
 */
class AkkaSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with StrictLogging {

  "Group A" >> {
    "Test 1" >> {
      val echo = system.actorOf(Props[StupidActor],"stupid")
      val msg = "Goodbye World"
      echo ! msg
      expectMsg(msg)
      success
    }
  }
}

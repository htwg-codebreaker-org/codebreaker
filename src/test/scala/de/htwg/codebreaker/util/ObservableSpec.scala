package de.htwg.codebreaker.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ObservableSpec extends AnyWordSpec with Matchers {

  class TestObserver extends Observer {
    var updated = false
    def reset(): Unit = updated = false
    override def update: Unit = updated = true
  }

  "An Observable" should {
    val observable = new Observable()
    val observer1 = new TestObserver()
    val observer2 = new TestObserver()

    "allow adding observers" in {
      observable.add(observer1)
      observable.subscribers should contain (observer1)
    }

    "allow removing observers" in {
      observable.add(observer2)
      observable.remove(observer2)
      observable.subscribers should not contain observer2
    }

    "notify all observers" in {
      observer1.reset()
      observable.notifyObservers
      observer1.updated should be (true)
    }

    "not notify removed observers" in {
      observer2.reset()
      observable.add(observer2)
      observable.remove(observer2)
      observable.notifyObservers
      observer2.updated should be (false)
    }
  }
}

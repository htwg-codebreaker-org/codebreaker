package de.htwg.codebreaker.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ObservableSpec extends AnyWordSpec with Matchers {

  class TestObserver extends Observer {
    var updated = false
    def reset(): Unit = updated = false
    override def update(): Unit = updated = true
  }

  "An Observable" should {

    "allow adding observers" in {
      val observable = new Observable()
      val observer = new TestObserver()
      observable.add(observer)
      observable.subscribers should contain(observer)
    }

    "allow removing observers" in {
      val observable = new Observable()
      val observer = new TestObserver()
      observable.add(observer)
      observable.remove(observer)
      observable.subscribers should not contain observer
    }

    "notify all observers" in {
      val observable = new Observable()
      val observer = new TestObserver()
      observable.add(observer)
      observer.reset()
      observable.notifyObservers
      observer.updated shouldBe true
    }

    "not notify removed observers" in {
      val observable = new Observable()
      val observer = new TestObserver()
      observable.add(observer)
      observable.remove(observer)
      observer.reset()
      observable.notifyObservers
      observer.updated shouldBe false
    }
  }
}

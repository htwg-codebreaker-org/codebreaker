package de.htwg.codebreaker.util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ObservableSpec extends AnyWordSpec with Matchers {

  // Test observer to track update() calls
  class TestObserver extends Observer {
    var updated                 = false
    def reset(): Unit           = updated = false
    override def update(): Unit = updated = true
  }

  // Concrete implementation of Observable for testing
  // This is needed because Observable is now a trait
  class TestObservable extends Observable {
    // Public method to trigger notifications for testing
    def triggerNotification(): Unit = notifyObservers
  }

  "An Observable" should {

    "allow adding observers" in {
      val observable = new TestObservable()
      val observer   = new TestObserver()
      observable.add(observer)
      // We can't directly access subscribers anymore (it's private),
      // so we verify by checking if the observer gets notified
      observable.triggerNotification()
      observer.updated shouldBe true
    }

    "allow removing observers" in {
      val observable = new TestObservable()
      val observer   = new TestObserver()
      observable.add(observer)
      observable.remove(observer)
      observer.reset()
      // After removal, observer should not be notified
      observable.triggerNotification()
      observer.updated shouldBe false
    }

    "notify all observers" in {
      val observable = new TestObservable()
      val observer1  = new TestObserver()
      val observer2  = new TestObserver()
      observable.add(observer1)
      observable.add(observer2)
      observer1.reset()
      observer2.reset()
      observable.triggerNotification()
      observer1.updated shouldBe true
      observer2.updated shouldBe true
    }

    "not notify removed observers" in {
      val observable = new TestObservable()
      val observer1  = new TestObserver()
      val observer2  = new TestObserver()
      observable.add(observer1)
      observable.add(observer2)
      observable.remove(observer1)
      observer1.reset()
      observer2.reset()
      observable.triggerNotification()
      observer1.updated shouldBe false
      observer2.updated shouldBe true
    }
  }
}

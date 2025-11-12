package de.htwg.codebreaker.util

/**
 * Observer trait for the Observer pattern.
 * Observers are notified when the observed object changes state.
 */
trait Observer {
  def update(): Unit
}

/**
 * Observable trait for the Observer pattern.
 * Allows objects to register as observers and be notified of changes.
 * Internal subscriber list is encapsulated and cannot be accessed directly.
 */
trait Observable {
  private var subscribers: Vector[Observer] = Vector()

  def add(s: Observer): Unit = subscribers = subscribers :+ s

  def remove(s: Observer): Unit = subscribers = subscribers.filterNot(o => o == s)

  protected def notifyObservers: Unit = subscribers.foreach(o => o.update())
}

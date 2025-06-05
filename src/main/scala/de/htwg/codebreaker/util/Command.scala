package de.htwg.codebreaker.util

trait Command {
  def execute(): Unit
  def undo(): Unit
}

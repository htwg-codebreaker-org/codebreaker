package de.htwg.codebreaker
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.view.Tui
import de.htwg.codebreaker.controller.Controller


object Codebreaker {
  val controller = new Controller()
  val tui = new Tui(controller)

  def main(args: Array[String]): Unit = {

    var input: String = ""
    while (input != "q") {
      print("> ")
      input = scala.io.StdIn.readLine()
      tui.processInputLine(input)
    }
    println("Danke fürs Spielen!")
  }
}
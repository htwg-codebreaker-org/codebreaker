// src/main/scala/de/htwg/codebreaker/Main.scala
package de.htwg.codebreaker

import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.view.{TUI}
import de.htwg.codebreaker.view.gui.GUI

object Codebreaker:
  def main(args: Array[String]): Unit =
    val game = GameFactory("default")
    val controller = Controller(game)

    // TUI und GUI als Observer registrieren
    val tui = new TUI(controller)
    val gui = new GUI(controller)

    // GUI in eigenem Thread starten
    new Thread(() => gui.main(Array.empty)).start()

    // TUI direkt anzeigen
    tui.show()

    var input = ""
    while input != "q" do
      print("> ")
      input = scala.io.StdIn.readLine()
      tui.processInputLine(input)
    sys.exit()

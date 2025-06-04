// src/main/scala/de/htwg/codebreaker/Main.scala
package de.htwg.codebreaker

import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.view.{TUI, GUI}

object Codebreaker:
  def main(args: Array[String]): Unit =
    val game = GameFactory("default")
    val controller = Controller(game)

    // TUI starten
    val tui = new TUI(controller)

    // GUI vorbereiten und in eigenem Thread starten
    GUI.controller = controller
    new Thread(() => GUI.main(Array.empty)).start()

    // Konsoleninterface
    var input = ""
    while input != "q" do
      print("> ")
      input = scala.io.StdIn.readLine()
      tui.processInputLine(input)

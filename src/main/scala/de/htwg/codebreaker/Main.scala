package de.htwg.codebreaker

import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.view.{TUI, GUI}

object Codebreaker:

  def main(args: Array[String]): Unit =
    val game = GameFactory("default")
    val controller = Controller(game)

    // Controller in beide Views einfügen
    GUI.init(controller)
    TUI.init(controller)

    // Thread für die TUI
    val tuiThread = new Thread(() =>
      var input = ""
      while input != "q" do
        print("> ")
        input = scala.io.StdIn.readLine()
        TUI.processInputLine(input)
    )

    // Thread für die GUI (JavaFX will GUI in eigenem Thread)
    val guiThread = new Thread(() =>
      GUI.main(Array.empty) // oder args, wenn nötig
    )

    // Beide Threads starten
    tuiThread.start()
    guiThread.start()

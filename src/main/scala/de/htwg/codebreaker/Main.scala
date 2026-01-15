// src/main/scala/de/htwg/codebreaker/Main.scala
package de.htwg.codebreaker

import com.google.inject.{Guice, Injector}
import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.module.CodebreakerModule
import de.htwg.codebreaker.view.TUI
import de.htwg.codebreaker.view.gui.GUI

/** Main entry point for the Codebreaker application. Demonstrates Dependency Injection with Google
  * Guice:
  *   - Creates a Guice Injector with CodebreakerModule
  *   - Injector automatically resolves and injects dependencies
  *   - Components are created through the injector, ensuring proper DI
  *   - All dependencies (Game, ControllerInterface) are injected automatically
  */
object Codebreaker:
  def main(args: Array[String]): Unit =
    // Create the Guice injector with our module
    val injector: Injector = Guice.createInjector(new CodebreakerModule())

    // Get controller to check for saved game
    val controller = injector.getInstance(classOf[ControllerInterface])

    // Try to load saved game, if it exists
    println("Versuche gespeichertes Spiel zu laden...")
    controller.load() // Will print success or error message

    // Get component instances from the injector
    // Guice automatically injects all dependencies (Game -> Controller, Controller -> Views)
    val tui = injector.getInstance(classOf[TUI])
    val gui = injector.getInstance(classOf[GUI])

    // GUI in eigenem Thread starten
    new Thread(() => gui.main(Array.empty)).start()

    tui.show()

    var input = ""
    while input != "q" do
      print("> ")
      input = scala.io.StdIn.readLine()
      tui.processInputLine(input)
    sys.exit()

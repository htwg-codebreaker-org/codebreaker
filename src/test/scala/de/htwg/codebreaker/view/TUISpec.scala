package de.htwg.codebreaker.view

import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.MapObject._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TUISpec extends AnyWordSpec with Matchers {

  // Einfaches Setup für das Model
  val tile = Tile(0, 0, Continent.Europe)

  val players = List(Player(0, "Tester", tile, 5, 5, 5, 1, 0, 50))
  val servers = List(Server("TestServer", tile, 10, 2, 3, false, ServerType.Side))

  val worldMap = WorldMap(1, 1, Vector(tile))
  val model = GameModel(players, servers, worldMap)
  val state = GameState()

  val controller = new Controller(model, state)
  val tui = new TUI(controller)

  "A TUI" should {

    "handle input: 'q' to quit" in {
      val output = captureOutput {
        tui.processInputLine("q")
      }
      output should include ("Spiel beendet.")
    }

    "handle input: 'm' to show full view" in {
      val output = captureOutput {
        tui.processInputLine("m")
      }
      output should include ("Willkommen zu Codebreaker!")
      output should include ("Spieler 0: Tester")
      output should include ("TestServer")
    }

    "handle unknown input" in {
      val output = captureOutput {
        tui.processInputLine("xyz")
      }
      output should include ("Unbekannter Befehl.")
    }

    "display all map object types" in {
      val data = Vector(Vector(
        PlayerAndServerTile(0, 0, ServerType.Side, Continent.Europe),
        PlayerOnTile(0),
        ServerOnTile(0, ServerType.Bank, Continent.Asia),
        EmptyTile(Continent.Africa)
      ))
      val mapString = tui.displayMap(data)
      mapString should include ("[P0/S0]")
      mapString should include ("[P0]")
      mapString should include ("[0]S-AS")
      mapString should include (" . ")
    }

    "print server list for all types" in {
      val allTypes = ServerType.values.toList.zipWithIndex.map { case (stype, idx) =>
        Server(s"Server$idx", tile, 20, 1, 2, false, stype)
      }
      val output = captureOutput {
        tui.printServerList(allTypes)
      }
      ServerType.values.foreach { stype =>
        output should include (stype.toString)
      }
    }
  }

  // Hilfsfunktion: println-Ausgaben abfangen
  def captureOutput(block: => Unit): String = {
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream)(block)
    stream.toString
  }
}

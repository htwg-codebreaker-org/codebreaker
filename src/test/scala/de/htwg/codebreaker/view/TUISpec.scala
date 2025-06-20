package de.htwg.codebreaker.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.controller._
import java.io.{ByteArrayOutputStream, PrintStream}

class TUISpec extends AnyWordSpec with Matchers {

  val tile = Tile(0, 0, Continent.Europe)
  val player = Player(0, "Tester", tile, 1, 1, 1, 1, 0, 0)
  val server = Server("S1", tile, 10, 2, 2, false, ServerType.Firm)
  val worldMap = WorldMap(1, 1, Vector(tile))
  val model = GameModel(List(player), List(server), worldMap)
  val state = GameState()
  val controller = Controller(Game(model, state))
  val tui = TUI(controller)

  def captureOutput(body: => Unit): String = {
    val outCapture = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outCapture)) {
      body
    }
    outCapture.toString.trim
  }

  "TUI" should {

    "print welcome and game info on show()" in {
      val output = captureOutput {
        tui.show()
      }
      output should include ("Willkommen zu Codebreaker")
      output should include ("Spieler 0: Tester")
      output should include ("S1")
    }

    "handle 'q' to quit" in {
      val output = captureOutput {
        tui.processInputLine("q")
      }
      output should include ("Spiel beendet")
    }

    "handle unknown input" in {
      val output = captureOutput {
        tui.processInputLine("xyz")
      }
      output should include ("Unbekannter Befehl")
    }

    "handle 'm' to show map" in {
      val output = captureOutput {
        tui.processInputLine("m")
      }
      output should include ("== Codebreaker: Weltkarte ==")
    }

    "handle 'help' to show commands" in {
      val output = captureOutput {
        tui.processInputLine("help")
      }
      output should include ("Verfügbare Befehle")
      output should include ("claim <S>")
    }

    "handle 'claim S1' command" in {
      val output = captureOutput {
        tui.processInputLine("claim S1")
      }
      controller.getServers.find(_.name == "S1").get.claimedBy should contain (0)
    }

    "handle 'undo' and 'redo'" in {
      tui.processInputLine("claim S1")
      controller.getServers.head.claimedBy shouldBe Some(0)

      tui.processInputLine("undo")
      controller.getServers.head.claimedBy shouldBe None

      tui.processInputLine("redo")
      controller.getServers.head.claimedBy shouldBe Some(0)
    }

    "handle 'next' to advance player" in {
      val output = captureOutput {
        tui.processInputLine("next")
      }
      output should include ("Spielzustand hat sich geändert")
    }

    "reject invalid 'claim' syntax" in {
      val output = captureOutput {
        tui.processInputLine("claim")
      }
      output should include ("Syntax: claim <Servername>")
    }
  }
}

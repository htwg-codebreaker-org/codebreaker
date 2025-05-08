package de.htwg.codebreaker.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.model._
import java.io.{ByteArrayOutputStream, PrintStream}

class TuiSpec extends AnyWordSpec with Matchers {

  "A Tui" should {

    val controller = new Controller()
    val tui = new Tui(controller)

    // Um Konsole abzufangen
    def withConsoleOut(testCode: => Unit): String = {
      val out = new ByteArrayOutputStream()
      Console.withOut(new PrintStream(out)) {
        testCode
      }
      out.toString.trim
    }

    "respond to 'q' with exit message" in {
      val output = withConsoleOut {
        tui.processInputLine("q")
      }
      output should include ("Spiel beendet.")
    }

    "create a new player when input starts with 'n '" in {
      val output = withConsoleOut {
        tui.processInputLine("n Alice")
      }
      output should include ("Spieler 'Alice' wurde erstellt")
    }

    "handle unknown command" in {
      val output = withConsoleOut {
        tui.processInputLine("xyz")
      }
      output should include ("Unbekannter Befehl.")
    }

    "print the map when 'm' is entered" in {
      val output = withConsoleOut {
        tui.processInputLine("m")
      }
      output should include ("Aktuelle Map:")
    }

    "update when notified" in {
      val output = withConsoleOut {
        tui.update
      }
      output should include ("tui is updated")
    }
  }
}


package de.htwg.codebreaker.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.controller.{ControllerInterface, TestGameFactory, FakeFileIO}
import de.htwg.codebreaker.controller.controller.Controller
import de.htwg.codebreaker.model.{MapObject, ServerType}
import de.htwg.codebreaker.model.MapObject._
import de.htwg.codebreaker.model.Continent

class TUISpec extends AnyWordSpec with Matchers {

  "TUI" should {

    "initialize with controller" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      tui should not be null
    }

    "display map with empty tiles" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val emptyMap = Vector(
        Vector(EmptyTile(Continent.NorthAmerica), EmptyTile(Continent.Europe)),
        Vector(EmptyTile(Continent.Asia), EmptyTile(Continent.Africa))
      )
      
      val result = tui.displayMap(emptyMap)
      result should not be empty
      result should include("NA")
      result should include("EU")
    }

    "display map with player on tile" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val mapWithPlayer = Vector(
        Vector(PlayerOnTile(0), EmptyTile(Continent.Europe))
      )
      
      val result = tui.displayMap(mapWithPlayer)
      result should include("P0")
    }

    "display map with server on tile" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val mapWithServer = Vector(
        Vector(ServerOnTile(0, ServerType.Side, Continent.NorthAmerica), EmptyTile(Continent.Europe))
      )
      
      val result = tui.displayMap(mapWithServer)
      result should include("00")
    }

    "display map with player and server on same tile" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val mapWithBoth = Vector(
        Vector(PlayerAndServerTile(0, 5, ServerType.Side, Continent.NorthAmerica))
      )
      
      val result = tui.displayMap(mapWithBoth)
      result should include("P0")
      result should include("05")
    }

    "use color codes for different tile types" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val coloredMap = Vector(
        Vector(
          PlayerOnTile(0),
          ServerOnTile(1, ServerType.Firm, Continent.Europe),
          PlayerAndServerTile(0, 2, ServerType.Side, Continent.Asia)
        )
      )
      
      val result = tui.displayMap(coloredMap)
      // Check for ANSI color codes
      result should include("\u001B[34m") // Blue for player
      result should include("\u001B[32m") // Green for server
      result should include("\u001B[31m") // Red for player+server
      result should include("\u001B[0m")  // Reset
    }

    "display multiple rows correctly" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val multiRowMap = Vector(
        Vector(EmptyTile(Continent.NorthAmerica), EmptyTile(Continent.Europe)),
        Vector(EmptyTile(Continent.Asia), EmptyTile(Continent.Africa)),
        Vector(EmptyTile(Continent.Oceania), EmptyTile(Continent.Antarctica))
      )
      
      val result = tui.displayMap(multiRowMap)
      val lines = result.split("\n")
      lines should have length 3
    }

    "format continent codes correctly" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val continentMap = Vector(
        Vector(
          EmptyTile(Continent.NorthAmerica),
          EmptyTile(Continent.SouthAmerica),
          EmptyTile(Continent.Europe),
          EmptyTile(Continent.Africa),
          EmptyTile(Continent.Asia),
          EmptyTile(Continent.Oceania),
          EmptyTile(Continent.Antarctica),
          EmptyTile(Continent.Ocean)
        )
      )
      
      val result = tui.displayMap(continentMap)
      result should include("NA") // NorthAmerica
      result should include("SA") // SouthAmerica
      result should include("EU") // Europe
      result should include("AF") // Africa
      result should include("AS") // Asia
      result should include("OC") // Oceania
      result should include("AN") // Antarctica
      result should include("~~") // Ocean
    }

    "handle show() without errors" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.show()
      }
    }

    "handle update() callback" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.update()
      }
    }

    "process quit command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("q")
      }
    }

    "process map command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("m")
      }
    }

    "process help command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("help")
      }
    }

    "process undo command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("undo")
      }
    }

    "process redo command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("redo")
      }
    }

    "process next command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val initialRound = controller.getState.round
      tui.processInputLine("next")
      // Command should be executed
    }

    "handle unknown commands gracefully" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("invalid_command")
      }
    }

    "handle empty input" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("")
      }
    }

    "trim whitespace from input" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      noException should be thrownBy {
        tui.processInputLine("  m  ")
      }
    }

    "format server index with leading zeros" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val mapWithServers = Vector(
        Vector(
          ServerOnTile(0, ServerType.Side, Continent.NorthAmerica),
          ServerOnTile(5, ServerType.Firm, Continent.Europe),
          ServerOnTile(15, ServerType.Cloud, Continent.Asia)
        )
      )
      
      val result = tui.displayMap(mapWithServers)
      result should include("00")
      result should include("05")
      result should include("15")
    }

    "handle large map without errors" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val tui = new TUI(controller)
      
      val largeMap = Vector.fill(40)(
        Vector.fill(80)(EmptyTile(Continent.Ocean))
      )
      
      noException should be thrownBy {
        val result = tui.displayMap(largeMap)
        result should not be empty
      }
    }
  }
}

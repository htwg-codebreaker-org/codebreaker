package de.htwg.codebreaker.view.gui

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.Color
import scalafx.beans.property.BooleanProperty
import de.htwg.codebreaker.controller.ClaimServerCommand

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.controller.Controller


// Testbare Subklasse von GUI, die keine echte Stage initialisiert
class TestGUI(controller: Controller) extends GUI(controller) {
  override def showWorldMap(): Unit = () // überspringt GUI-Rendering
  override def start(): Unit = () // blockiert das GUI-Fenster vollständig
}


class GUISpec extends AnyWordSpec with Matchers {

  val tile = Tile(0, 0, Continent.Europe)
  val player = Player(0, "Tester", tile, 1, 1, 1, 1, 0, 0)
  val server = Server("S1", tile, 10, 2, 2, false, ServerType.Bank)
  val worldMap = WorldMap(1, 1, Vector(tile))
  val model = GameModel(List(player), List(server), worldMap)
  val state = GameState()
  val controller = Controller(Game(model, state))
  val gui = new TestGUI(controller)

  "A GUI" should {

    "map all continents to valid colors" in {
      Continent.values.foreach { c =>
        val color = gui.continentColor(c)
        color shouldBe a [Color]
      }
    }

    "map all server types to valid icon file paths" in {
      ServerType.values.foreach { t =>
        val path = gui.serverIconFile(t)
        path should include ("assets/graphics/server/icons/")
        assert(
          path.endsWith(".png") || path.endsWith("player_base.png"),
          s"Unexpected path: $path"
        )

      }
    }

    "initialize and update undo/redo properties" in {
      // Initialisiere GUI-Properties korrekt
      gui.startGame()

      // Einen Server claimen → erzeugt Eintrag im Undo-Stack
      val claimCmd = ClaimServerCommand("S1", 0)
      controller.doAndRemember(claimCmd)

      noException should be thrownBy gui.update()

      gui.canUndoProperty.value shouldBe true  // undoStack ist jetzt belegt
      gui.canRedoProperty.value shouldBe false
    }



    "start game and set correct mode" in {
      gui.canUndoProperty = BooleanProperty(false)
      gui.canRedoProperty = BooleanProperty(false)

      noException should be thrownBy gui.startGame()
      gui.controller.getState.currentPlayerIndex.isDefined shouldBe true
    }

  }
}

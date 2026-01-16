package de.htwg.codebreaker.view.gui.components.menu.skilltree

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{VBox, HBox}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.UnlockSkillCommand
import de.htwg.codebreaker.model.{HackSkill, Player}

class SkillTreeWindow(
  controller: ControllerInterface
) {

  def show(): Unit = {
    val stage = new Stage {
      title = "Skilltree"
      resizable = false
    }

    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player: Player = controller.getPlayers(playerIndex)
    val allSkills: List[HackSkill] = controller.game.model.skills

    val header = new Label(
      s"${player.name} – Skilltree (XP: ${player.availableXp})"
    ) {
      style = "-fx-font-size: 16px; -fx-font-weight: bold;"
    }

    val skillNodes = allSkills.map { skill =>
      createSkillRow(skill, player, playerIndex, stage)
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 10
        padding = Insets(20)
        alignment = Pos.TopCenter
        children = Seq(header) ++ skillNodes
      }
    )

    stage.show()
  }

  private def createSkillRow(
    skill: HackSkill,
    player: Player,
    playerIndex: Int,
    stage: Stage
  ): HBox = {

    val unlocked = player.skills.unlockedSkillIds.contains(skill.id)
    val canUnlock = player.availableXp >= skill.costXp

    val label = new Label(
      s"${skill.name} – ${skill.description} (XP: ${skill.costXp})"
    )

    val button = new Button {
      if (unlocked) {
        text = "✔ Freigeschaltet"
        disable = true
      } else if (!canUnlock) {
        text = "❌ Zu wenig XP"
        disable = true
      } else {
        text = "🔓 Freischalten"
        onAction = _ => {
          controller.doAndRemember(
            UnlockSkillCommand(playerIndex, skill)
          )
          stage.close()
          show() // Fenster neu öffnen → aktualisierte XP / Skills
        }
      }
    }

    new HBox {
      spacing = 15
      alignment = Pos.CenterLeft
      children = Seq(label, button)
    }
  }
}

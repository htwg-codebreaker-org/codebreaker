package de.htwg.codebreaker.view.gui.components.menu.hack

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ProgressIndicator}
import scalafx.scene.layout.{VBox, HBox}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.animation.{PauseTransition}
import scalafx.util.Duration

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.HackServerCommand
import de.htwg.codebreaker.model.{Server, HackSkill, Player}

class AttackSelectionWindow(
  controller: ControllerInterface,
  server: Server,
  playerIndex: Int
) {

  def show(): Unit = {
    val stage = new Stage {
      title = s"Hack: ${server.name}"
      resizable = false
    }

    val player: Player = controller.getPlayers(playerIndex)

    val availableSkills =
      controller.game.model.skills.filter { skill =>
        player.skills.unlockedSkillIds.contains(skill.id)
      }

    val header = new Label(
      s"${player.name} → ${server.name}\nSchwierigkeit: ${server.difficulty}"
    ) {
      style = "-fx-font-size: 16px; -fx-font-weight: bold;"
    }

    val skillRows = availableSkills.map { skill =>
      createSkillRow(skill, player, stage)
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 12
        padding = Insets(20)
        alignment = Pos.TopCenter
        children = Seq(header) ++ skillRows
      }
    )

    stage.show()
  }

  private def createSkillRow(
    skill: HackSkill,
    player: Player,
    stage: Stage
  ): HBox = {

    val baseChance = 100 - server.difficulty
    val securityBonus = player.cybersecurity / 2
    val totalChance =
      math.max(5, math.min(95, baseChance + securityBonus + skill.successBonus))

    val label = new Label(
      s"${skill.name} (+${skill.successBonus}%) – Erfolg: $totalChance%"
    )

    val attackButton = new Button("▶ Angriff starten") {
      onAction = _ => {
        stage.close()
        playAttackAnimation(skill, totalChance)
      }
    }

    new HBox {
      spacing = 15
      alignment = Pos.CenterLeft
      children = Seq(label, attackButton)
    }
  }

  private def playAttackAnimation(skill: HackSkill, chance: Int): Unit = {
    val stage = new Stage {
      title = "Angriff läuft…"
      resizable = false
    }

    val indicator = new ProgressIndicator {
      progress = -1
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 15
        padding = Insets(20)
        alignment = Pos.Center
        children = Seq(
          new Label(s"${skill.name} wird ausgeführt…"),
          indicator
        )
      }
    )

    stage.show()

    val pause = new PauseTransition(Duration(1200))
    pause.onFinished = _ => {
      stage.close()
      controller.doAndRemember(
        HackServerCommand(server.name, playerIndex, skill)
      )
    }
    pause.play()
  }
}

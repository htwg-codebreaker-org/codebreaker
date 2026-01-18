package de.htwg.codebreaker.view.gui.components.menu.skilltree

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{VBox, HBox, GridPane}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.{UnlockHackSkillCommand, UnlockSocialSkillCommand}
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.skill.{HackSkill, SocialSkill}

class SkillTreeWindow(
  controller: ControllerInterface
) {

  def show(): Unit = {
    val stage = new Stage {
      title = "Skilltree"
      resizable = false
      width = 800
      height = 600
    }

    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player: Player = controller.getPlayers(playerIndex)
    val allHackSkills: List[HackSkill] = controller.game.model.hackSkills
    val allSocialSkills: List[SocialSkill] = controller.game.model.socialSkills

    val header = new Label(
      s"${player.name} – Skilltree (XP: ${player.availableXp})"
    ) {
      style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10;"
    }

    // Zwei Spalten: Hack Skills | Social Skills
    val skillGrid = new GridPane {
      hgap = 30
      vgap = 10
      padding = Insets(20)

      // === HACK SKILLS (linke Spalte) ===
      val hackHeader = new Label("💻 Hack Skills") {
        style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4db8ff;"
      }
      add(hackHeader, 0, 0)

      allHackSkills.zipWithIndex.foreach { case (skill, idx) =>
        add(createHackSkillRow(skill, player, playerIndex, stage), 0, idx + 1)
      }

      // === SOCIAL SKILLS (rechte Spalte) ===
      val socialHeader = new Label("🗣️ Social Skills") {
        style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffcc00;"
      }
      add(socialHeader, 1, 0)

      allSocialSkills.zipWithIndex.foreach { case (skill, idx) =>
        add(createSocialSkillRow(skill, player, playerIndex, stage), 1, idx + 1)
      }
    }

    val closeButton = new Button("Schließen") {
      onAction = _ => stage.close()
      style = "-fx-background-color: #555; -fx-text-fill: white; -fx-font-weight: bold;"
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 10
        padding = Insets(20)
        alignment = Pos.TopCenter
        children = Seq(header, skillGrid, closeButton)
      }
    )

    stage.show()
  }

  // === HACK SKILL ROW ===
  private def createHackSkillRow(
    skill: HackSkill,
    player: Player,
    playerIndex: Int,
    stage: Stage
  ): VBox = {

    val unlocked = player.skills.unlockedHackSkills.contains(skill.id)
    val canUnlock = player.availableXp >= skill.costXp

    val nameLabel = new Label(s"${skill.name}") {
      style = "-fx-font-weight: bold; -fx-font-size: 14px;"
    }

    val descLabel = new Label(s"${skill.description}") {
      style = "-fx-font-size: 12px; -fx-text-fill: #aaa;"
      wrapText = true
      maxWidth = 300
    }

    val costLabel = new Label(s"Kosten: ${skill.costXp} XP | Bonus: +${skill.successBonus}%") {
      style = "-fx-font-size: 11px; -fx-text-fill: #4db8ff;"
    }

    val button = new Button {
      if (unlocked) {
        text = "✔ Freigeschaltet"
        disable = true
        style = "-fx-background-color: #66ff66; -fx-text-fill: black;"
      } else if (!canUnlock) {
        text = "❌ Zu wenig XP"
        disable = true
        style = "-fx-background-color: #555; -fx-text-fill: white;"
      } else {
        text = "🔓 Freischalten"
        style = "-fx-background-color: #4db8ff; -fx-text-fill: white; -fx-font-weight: bold;"
        onAction = _ => {
          controller.doAndRemember(
            UnlockHackSkillCommand(playerIndex, skill)
          )
          stage.close()
          show() // Fenster neu öffnen
        }
      }
    }

    new VBox {
      spacing = 5
      padding = Insets(10)
      style = "-fx-border-color: #4db8ff; -fx-border-width: 2; -fx-background-color: #222;"
      children = Seq(nameLabel, descLabel, costLabel, button)
    }
  }

  // === SOCIAL SKILL ROW ===
  private def createSocialSkillRow(
    skill: SocialSkill,
    player: Player,
    playerIndex: Int,
    stage: Stage
  ): VBox = {

    val unlocked = player.skills.unlockedSocialSkills.contains(skill.id)
    val canUnlock = player.availableXp >= skill.costXp

    val nameLabel = new Label(s"${skill.name}") {
      style = "-fx-font-weight: bold; -fx-font-size: 14px;"
    }

    val descLabel = new Label(s"${skill.description}") {
      style = "-fx-font-size: 12px; -fx-text-fill: #aaa;"
      wrapText = true
      maxWidth = 300
    }

    val costLabel = new Label(s"Kosten: ${skill.costXp} XP | Bonus: +${skill.successBonus}%") {
      style = "-fx-font-size: 11px; -fx-text-fill: #ffcc00;"
    }

    val button = new Button {
      if (unlocked) {
        text = "✔ Freigeschaltet"
        disable = true
        style = "-fx-background-color: #66ff66; -fx-text-fill: black;"
      } else if (!canUnlock) {
        text = "❌ Zu wenig XP"
        disable = true
        style = "-fx-background-color: #555; -fx-text-fill: white;"
      } else {
        text = "🔓 Freischalten"
        style = "-fx-background-color: #ffcc00; -fx-text-fill: black; -fx-font-weight: bold;"
        onAction = _ => {
          controller.doAndRemember(
            UnlockSocialSkillCommand(playerIndex, skill)
          )
          stage.close()
          show() // Fenster neu öffnen
        }
      }
    }

    new VBox {
      spacing = 5
      padding = Insets(10)
      style = "-fx-border-color: #ffcc00; -fx-border-width: 2; -fx-background-color: #222;"
      children = Seq(nameLabel, descLabel, costLabel, button)
    }
  }
}
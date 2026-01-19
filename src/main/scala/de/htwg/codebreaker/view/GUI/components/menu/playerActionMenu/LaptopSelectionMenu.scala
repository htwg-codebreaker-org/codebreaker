package de.htwg.codebreaker.view.gui.components.menu.playerActionMenu

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ProgressIndicator}
import scalafx.scene.layout.{VBox, HBox}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.animation.PauseTransition
import scalafx.util.Duration

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.{StartLaptopActionCommand, CollectLaptopActionResultCommand}
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.{LaptopAction, LaptopActionType}

/**
 * Window für die Auswahl von Laptop-Actions.
 * Zeigt verfügbare Actions und laufende Actions an.
 */
class LaptopActionSelectionMenu(
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
    val currentRound = controller.game.state.round

    val header = new Label(
      s"${player.name} → ${server.name}\n" +
      s"Schwierigkeit: ${server.difficulty}\n" +
      s"CPU: ${player.laptop.hardware.cpu} | RAM: ${player.laptop.hardware.ram} | Kerne frei: ${player.laptop.hardware.kerne}"
    ) {
      style = "-fx-font-size: 14px; -fx-font-weight: bold;"
    }

    // Hole alle verfügbaren Actions basierend auf den installierten Tools des Spielers
    val availableActions = getAvailableActionsForPlayer(player)

    // Laufende Actions für diesen Server
    val runningActionsForServer = player.laptop.runningActions.filter(_.targetServer == server.name)

    val actionRows = availableActions.map { action =>
      createActionRow(action, player, stage)
    }

    val runningRows = if (runningActionsForServer.nonEmpty) {
      Seq(
        new Label("═══ Laufende Actions ═══") {
          style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: orange;"
        }
      ) ++ runningActionsForServer.map { running =>
        createRunningActionRow(running, player, currentRound, stage)
      }
    } else Seq.empty

    stage.scene = new Scene(
      new VBox {
        spacing = 12
        padding = Insets(20)
        alignment = Pos.TopCenter
        children = Seq(header) ++ runningRows ++ Seq(
          new Label("═══ Neue Actions starten ═══") {
            style = "-fx-font-size: 14px; -fx-font-weight: bold;"
          }
        ) ++ actionRows
      }
    )

    stage.show()
  }

  /**
   * Holt alle verfügbaren Actions basierend auf den installierten Tools des Spielers
   */
  private def getAvailableActionsForPlayer(player: Player): List[LaptopAction] = {
    val installedToolIds = player.laptop.tools.toolIds
    val allTools = controller.game.model.laptopTools
    
    // Filtere nur die installierten Tools und sammle deren Actions
    allTools
      .filter(tool => installedToolIds.contains(tool.id))
      .flatMap(tool => tool.availableActions)
  }

  private def createActionRow(
    action: LaptopAction,
    player: Player,
    stage: Stage
  ): HBox = {

    val canAfford = 
      player.laptop.hardware.cpu >= action.cpuCost &&
      player.laptop.hardware.ram >= action.ramCost &&
      player.laptop.hardware.kerne >= action.coreCost

    val label = new Label(
      s"${action.name} (${action.durationRounds}R)\n" +
      s"  Kosten: ${action.cpuCost} CPU, ${action.ramCost} RAM, ${action.coreCost} Kerne"
    ) {
      style = if (canAfford) "-fx-text-fill: green;" else "-fx-text-fill: red;"
    }

    val startButton = new Button("▶ Starten") {
      disable = !canAfford
      onAction = _ => {
        stage.close()
        playStartAnimation(action)
      }
    }

    new HBox {
      spacing = 15
      alignment = Pos.CenterLeft
      children = Seq(label, startButton)
    }
  }

  private def createRunningActionRow(
    running: de.htwg.codebreaker.model.player.laptop.RunningLaptopAction,
    player: Player,
    currentRound: Int,
    stage: Stage
  ): HBox = {

    val roundsLeft = running.completionRound - currentRound
    val isReady = roundsLeft <= 0

    val status = if (isReady) "✓ FERTIG" else s"⏳ ${roundsLeft}R"
    val color = if (isReady) "green" else "orange"

    val label = new Label(
      s"${running.action.name} → ${status}"
    ) {
      style = s"-fx-text-fill: $color; -fx-font-weight: bold;"
    }

    val collectButton = new Button(if (isReady) "📦 Ergebnis abholen" else "Läuft...") {
      disable = !isReady
      onAction = _ => {
        stage.close()
        showResultChoiceWindow(running.targetServer)
      }
    }

    new HBox {
      spacing = 15
      alignment = Pos.CenterLeft
      children = Seq(label, collectButton)
    }
  }

  private def playStartAnimation(action: LaptopAction): Unit = {
    val stage = new Stage {
      title = "Action wird gestartet…"
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
          new Label(s"${action.name} wird gestartet…"),
          indicator
        )
      }
    )

    stage.show()

    val pause = new PauseTransition(Duration(800))
    pause.onFinished = _ => {
      stage.close()
      controller.doAndRemember(
        StartLaptopActionCommand(playerIndex, action, server.name)
      )
    }
    pause.play()
  }

  private def showResultChoiceWindow(targetServer: String): Unit = {
    val stage = new Stage {
      title = "Hack erfolgreich!"
      resizable = false
    }

    val claimButton = new Button("🏴 Server claimen") {
      onAction = _ => {
        stage.close()
        controller.doAndRemember(
          CollectLaptopActionResultCommand(playerIndex, targetServer, claimServer = true)
        )
      }
    }

    val stealButton = new Button("💰 Nur Daten klauen (+XP)") {
      onAction = _ => {
        stage.close()
        controller.doAndRemember(
          CollectLaptopActionResultCommand(playerIndex, targetServer, claimServer = false)
        )
      }
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 15
        padding = Insets(20)
        alignment = Pos.Center
        children = Seq(
          new Label("Hack erfolgreich!") {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;"
          },
          new Label("Was möchtest du tun?"),
          claimButton,
          stealButton
        )
      }
    )

    stage.show()
  }
}
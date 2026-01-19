package de.htwg.codebreaker.view.gui.components.menu.playerActionMenu

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{VBox, HBox, BorderPane, Priority}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.TextAlignment

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.CollectLaptopActionResultCommand
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.RunningLaptopAction

/**
 * Hauptmenü für Laptop-Aktionen - 2 Spalten Layout
 * - Linke Spalte: Laptop Info, Attack, Running Tasks, Completed Tasks
 * - Rechte Spalte: Geclaimte Server
 */
class LaptopMainMenu(
  controller: ControllerInterface,
  playerIndex: Int
) {

  def show(): Unit = {
    val stage = new Stage {
      title = s"💻 Laptop"
      width = 900
      height = 600
      resizable = true
    }

    val player = controller.getPlayers(playerIndex)
    val currentRound = controller.game.state.round
    
    val runningCount = player.laptop.runningActions.count(_.completionRound > currentRound)
    val completedCount = controller.getCompletedActionsForCurrentPlayer()
      .count(_.completionRound <= currentRound)

    // === HEADER ===
    val header = new Label(s"${player.name} – Laptop") {
      style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10;"
      textAlignment = TextAlignment.Center
    }

    // === LINKE SPALTE: AKTIONEN ===
    val leftColumn = createActionsColumn(player, runningCount, completedCount, stage)

    // === RECHTE SPALTE: GECLAIMTE SERVER ===
    val rightColumn = createClaimedServersColumn()

    // === MAIN LAYOUT ===
    stage.scene = new Scene(
      new BorderPane {
        top = header
        center = new HBox {
          spacing = 15
          padding = Insets(10)
          children = Seq(leftColumn, rightColumn)
          HBox.setHgrow(leftColumn, Priority.Always)
          HBox.setHgrow(rightColumn, Priority.Always)
        }
      }
    )

    stage.show()
  }

  // ==========================================
  // LINKE SPALTE: AKTIONEN
  // ==========================================

  private def createActionsColumn(
    player: Player,
    runningCount: Int,
    completedCount: Int,
    parentStage: Stage
  ): VBox = {
    
    // Laptop Hardware Info
    val hardwareInfo = new VBox {
      style = "-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;"
      spacing = 5
      children = Seq(
        new Label("⚙️ Hardware") {
          style = "-fx-text-fill: #4db8ff; -fx-font-weight: bold; -fx-font-size: 13px;"
        },
        new HBox {
          spacing = 10
          children = Seq(
            new Label(s"CPU: ${player.laptop.hardware.cpu}") {
              style = "-fx-text-fill: #66ff66; -fx-font-size: 11px;"
            },
            new Label(s"RAM: ${player.laptop.hardware.ram}") {
              style = "-fx-text-fill: #66ccff; -fx-font-size: 11px;"
            },
            new Label(s"Code: ${player.laptop.hardware.code}") {
              style = "-fx-text-fill: #ffcc66; -fx-font-size: 11px;"
            }
          )
        },
        new Label(s"Kerne: ${player.laptop.hardware.kerne}") {
          style = "-fx-text-fill: #ccc; -fx-font-size: 11px;"
        }
      )
    }

    val attackButton = new Button("🔨 Angriff") {
      style = "-fx-font-size: 13px; -fx-padding: 12; -fx-background-color: #7b68ee; -fx-text-fill: white; -fx-font-weight: bold;"
      maxWidth = Double.MaxValue
      onAction = _ => {
        parentStage.close()
        showServerSelectionForAttack()
      }
    }

    val runningButton = new Button(s"🔄 Running Tasks ($runningCount)") {
      style = "-fx-font-size: 13px; -fx-padding: 12; -fx-background-color: #ff8c00; -fx-text-fill: white;"
      maxWidth = Double.MaxValue
      onAction = _ => showRunningTasksWindow(parentStage)
    }

    val completedButton = new Button(s"✅ Completed Tasks ($completedCount)") {
      style = "-fx-font-size: 13px; -fx-padding: 12; -fx-background-color: #32cd32; -fx-text-fill: white;"
      maxWidth = Double.MaxValue
      onAction = _ => showCompletedTasksWindow(parentStage)
    }

    val closeButton = new Button("❌ Schließen") {
      style = "-fx-font-size: 12px; -fx-padding: 10; -fx-background-color: #dc143c; -fx-text-fill: white;"
      maxWidth = Double.MaxValue
      onAction = _ => parentStage.close()
    }

    new VBox {
      style = "-fx-border-color: #444; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: #1a1a1a;"
      spacing = 12
      children = Seq(
        new Label("═══ Laptop Menu ═══") {
          style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4db8ff;"
        },
        hardwareInfo,
        attackButton,
        runningButton,
        completedButton,
        closeButton
      )
    }
  }

  // ==========================================
  // SERVER AUSWAHL FÜR ANGRIFF
  // ==========================================

  private def showServerSelectionForAttack(): Unit = {
    val player = controller.getPlayers(playerIndex)
    val playerTile = player.tile
    
    // Server nur auf aktuellem Tile (später: +WLAN Reichweite)
    val inRangeServers = controller.getServers.filter(_.tile == playerTile)

    if (inRangeServers.isEmpty) {
      // Info-Fenster: Kein Server in Reichweite
      val stage = new Stage {
        title = "⚠️ Kein Server in Reichweite"
        width = 400
        height = 200
        resizable = false
      }

      stage.scene = new Scene(
        new VBox {
          spacing = 15
          padding = Insets(20)
          alignment = Pos.Center
          children = Seq(
            new Label("📡 Kein Server in Reichweite") {
              style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            },
            new Label(s"Du befindest dich auf (${playerTile.x}, ${playerTile.y})") {
              style = "-fx-font-size: 12px; -fx-text-fill: #666;"
            },
            new Label("Bewege dich zu einem Server-Feld um angreifen zu können.") {
              style = "-fx-font-size: 11px; -fx-text-fill: #888;"
              wrapText = true
            },
            new Button("OK") {
              maxWidth = Double.MaxValue
              onAction = _ => stage.close()
            }
          )
        }
      )
      stage.show()
    } else if (inRangeServers.size == 1) {
      // Nur ein Server: direkt öffnen
      new LaptopActionSelectionMenu(controller, inRangeServers.head, playerIndex).show()
    } else {
      // Mehrere Server: Auswahl anzeigen
      val stage = new Stage {
        title = "🔨 Server wählen"
        width = 600
        height = 400
        resizable = true
      }

      val serverButtons = inRangeServers.map { server =>
        new Button(s"${server.name} (${server.difficulty}%) - ${server.serverType}") {
          style = "-fx-font-size: 12px; -fx-padding: 10;"
          maxWidth = Double.MaxValue
          onAction = _ => {
            stage.close()
            new LaptopActionSelectionMenu(controller, server, playerIndex).show()
          }
        }
      }

      val scrollPane = new ScrollPane {
        content = new VBox {
          spacing = 8
          padding = Insets(10)
          children = serverButtons
        }
        fitToWidth = true
      }

      stage.scene = new Scene(
        new VBox {
          spacing = 10
          padding = Insets(10)
          children = Seq(
            new Label(s"${inRangeServers.size} Server in Reichweite - Wähle einen:") {
              style = "-fx-font-size: 14px; -fx-font-weight: bold;"
            },
            scrollPane
          )
        }
      )

      stage.show()
    }
  }

  // ==========================================
  // RECHTE SPALTE: GECLAIMTE SERVER
  // ==========================================

  private def createClaimedServersColumn(): VBox = {
    val claimedServers = controller.getServers.filter(_.claimedBy.contains(playerIndex))

    val headerLabel = new Label("═══ Geclaimte Server ═══") {
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4db8ff;"
    }

    if (claimedServers.isEmpty) {
      new VBox {
        style = "-fx-border-color: #444; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: #1a1a1a;"
        spacing = 10
        children = Seq(
          headerLabel,
          new Label("Keine Server geclaimed") {
            style = "-fx-text-fill: #888; -fx-font-size: 12px;"
          }
        )
      }
    } else {
      val serverBoxes = claimedServers.map(createServerBox)

      val scrollPane = new ScrollPane {
        content = new VBox {
          spacing = 8
          children = serverBoxes
        }
        fitToWidth = true
        style = "-fx-background: #1a1a1a; -fx-background-color: #1a1a1a;"
      }

      new VBox {
        style = "-fx-border-color: #444; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: #1a1a1a;"
        spacing = 10
        children = Seq(headerLabel, scrollPane)
        VBox.setVgrow(scrollPane, Priority.Always)
      }
    }
  }

  private def createServerBox(s: Server): VBox = {
    val typeColor = s.serverType.toString match {
      case "Side" => "#66ff66"
      case "Firm" => "#66ccff"
      case "Cloud" => "#ffcc66"
      case "Bank" => "#ffb84d"
      case "Military" => "#ff6666"
      case "GKS" => "#ff99ff"
      case _ => "#888"
    }

    new VBox {
      style = s"-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: $typeColor; -fx-border-width: 2;"
      spacing = 5
      children = Seq(
        new Label(s"💻 ${s.name}") {
          style = s"-fx-text-fill: $typeColor; -fx-font-weight: bold; -fx-font-size: 12px;"
        },
        new Label(s"${s.serverType}") {
          style = "-fx-text-fill: #ccc; -fx-font-size: 11px;"
        },
        new Label(s"Schwierigkeit: ${s.difficulty}%") {
          style = "-fx-text-fill: #888; -fx-font-size: 10px;"
        },
        new Label(s"Position: (${s.tile.x}, ${s.tile.y})") {
          style = "-fx-text-fill: #888; -fx-font-size: 10px;"
        }
      )
    }
  }

  // ==========================================
  // RUNNING TASKS WINDOW
  // ==========================================

  private def showRunningTasksWindow(parentStage: Stage): Unit = {
    val stage = new Stage {
      title = "🔄 Laufende Actions"
      width = 700
      height = 400
      resizable = true
    }

    val player = controller.getPlayers(playerIndex)
    val currentRound = controller.game.state.round
    val running = player.laptop.runningActions.filter(_.completionRound > currentRound)

    if (running.isEmpty) {
      val noTasksLabel = new Label("🔄 Keine laufenden Actions") {
        style = "-fx-font-size: 14px;"
        textAlignment = TextAlignment.Center
        wrapText = true
      }

      stage.scene = new Scene(
        new VBox {
          spacing = 15
          padding = Insets(20)
          alignment = Pos.Center
          children = Seq(noTasksLabel)
        }
      )
    } else {
      val taskRows = running.map { action =>
        createRunningTaskRow(action, player, stage)
      }

      val scrollPane = new ScrollPane {
        content = new VBox {
          spacing = 10
          padding = Insets(10)
          children = taskRows
        }
        fitToWidth = true
      }

      stage.scene = new Scene(
        new BorderPane {
          top = new Label("🔄 Laufende Actions") {
            style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;"
          }
          center = scrollPane
        }
      )
    }

    stage.show()
  }

  private def createRunningTaskRow(
    action: RunningLaptopAction,
    player: Player,
    stage: Stage
  ): HBox = {
    val currentRound = controller.game.state.round
    val roundsLeft = action.completionRound - currentRound
    val isReady = roundsLeft <= 0

    val taskInfo = new VBox {
      spacing = 5
      children = Seq(
        new Label(s"${action.action.name}") {
          style = "-fx-font-size: 13px; -fx-font-weight: bold;"
        },
        new Label(s"Target: ${action.targetServer}") {
          style = "-fx-font-size: 11px; -fx-text-fill: #666;"
        },
        new Label(s"Fertig in Runde: ${action.completionRound}") {
          style = "-fx-font-size: 11px;"
        },
        new Label(action.action.description) {
          style = "-fx-font-size: 10px; -fx-text-fill: #888;"
          wrapText = true
        }
      )
    }

    val status = if (isReady) "✓ FERTIG" else s"⏳ ${roundsLeft}R"
    val statusColor = if (isReady) "green" else "orange"

    val statusLabel = new Label(status) {
      style = s"-fx-text-fill: $statusColor; -fx-font-weight: bold; -fx-font-size: 12px;"
    }

    val collectButton = new Button(if (isReady) "📦 Abholen" else "⏳") {
      style = "-fx-font-size: 11px; -fx-padding: 8;"
      disable = !isReady
      onAction = _ => {
        stage.close()
        showResultChoiceWindow(action.targetServer)
      }
    }

    new HBox {
      spacing = 15
      padding = Insets(10)
      style = "-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;"
      children = Seq(taskInfo, statusLabel, collectButton)
      HBox.setHgrow(taskInfo, Priority.Always)
    }
  }

  // ==========================================
  // COMPLETED TASKS WINDOW
  // ==========================================

  private def showCompletedTasksWindow(parentStage: Stage): Unit = {
    val stage = new Stage {
      title = "✅ Abgeschlossene Actions"
      width = 700
      height = 400
      resizable = true
    }

    val currentRound = controller.game.state.round
    val completed = controller.getCompletedActionsForCurrentPlayer()
      .filter(_.completionRound <= currentRound)

    if (completed.isEmpty) {
      val noTasksLabel = new Label("✅ Keine abgeschlossenen Actions") {
        style = "-fx-font-size: 14px;"
        textAlignment = TextAlignment.Center
        wrapText = true
      }

      stage.scene = new Scene(
        new VBox {
          spacing = 15
          padding = Insets(20)
          alignment = Pos.Center
          children = Seq(noTasksLabel)
        }
      )
    } else {
      val taskRows = completed.map { action =>
        createCompletedTaskRow(action.action.name, action.targetServer, action.action.description, stage)
      }

      val scrollPane = new ScrollPane {
        content = new VBox {
          spacing = 10
          padding = Insets(10)
          children = taskRows
        }
        fitToWidth = true
      }

      stage.scene = new Scene(
        new BorderPane {
          top = new Label("✅ Abgeschlossene Actions") {
            style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;"
          }
          center = scrollPane
        }
      )
    }

    stage.show()
  }

  private def createCompletedTaskRow(
    actionName: String,
    targetServer: String,
    description: String,
    parentStage: Stage
  ): HBox = {

    val taskInfo = new VBox {
      spacing = 5
      children = Seq(
        new Label(actionName) {
          style = "-fx-font-size: 13px; -fx-font-weight: bold;"
        },
        new Label(s"Target: $targetServer") {
          style = "-fx-font-size: 11px; -fx-text-fill: #666;"
        },
        new Label(description) {
          style = "-fx-font-size: 10px; -fx-text-fill: #888;"
          wrapText = true
        }
      )
    }

    val statusLabel = new Label("✓ FERTIG") {
      style = "-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 12px;"
    }

    val collectButton = new Button("🏆") {
      style = "-fx-font-size: 11px; -fx-padding: 8;"
      onAction = _ => {
        parentStage.close()
        showResultChoiceWindow(targetServer)
      }
    }

    new HBox {
      spacing = 15
      padding = Insets(10)
      style = "-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f0f8f0;"
      children = Seq(taskInfo, statusLabel, collectButton)
      HBox.setHgrow(taskInfo, Priority.Always)
    }
  }

  // ==========================================
  // CLAIM vs STEAL WINDOW
  // ==========================================

  private def showResultChoiceWindow(targetServer: String): Unit = {
    val stage = new Stage {
      title = "Hack erfolgreich!"
      width = 500
      height = 300
      resizable = false
    }

    val titleLabel = new Label("🎉 Hack erfolgreich!") {
      style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: green;"
    }

    val questionLabel = new Label("Was möchtest du tun?") {
      style = "-fx-font-size: 14px;"
    }

    val claimButton = new Button("🏆 Claim\n(Server beanspruchen + Belohnung)") {
      style = "-fx-font-size: 12px; -fx-padding: 15; -fx-wrap-text: true;"
      wrapText = true
      maxWidth = Double.MaxValue
      prefHeight = 60
      onAction = _ => {
        stage.close()
        controller.doAndRemember(
          CollectLaptopActionResultCommand(playerIndex, targetServer, claimServer = true)
        )
      }
    }

    val stealButton = new Button("🔓 Daten klauen\n(nur Belohnung, nicht geclaimed)") {
      style = "-fx-font-size: 12px; -fx-padding: 15; -fx-wrap-text: true;"
      wrapText = true
      maxWidth = Double.MaxValue
      prefHeight = 60
      onAction = _ => {
        stage.close()
        controller.doAndRemember(
          CollectLaptopActionResultCommand(playerIndex, targetServer, claimServer = false)
        )
      }
    }

    val cancelButton = new Button("❌ Abbrechen") {
      style = "-fx-font-size: 12px; -fx-padding: 10;"
      maxWidth = Double.MaxValue
      onAction = _ => stage.close()
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 15
        padding = Insets(20)
        alignment = Pos.TopCenter
        children = Seq(
          titleLabel,
          questionLabel,
          claimButton,
          stealButton,
          cancelButton
        )
      }
    )

    stage.show()
  }
}
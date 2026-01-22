package de.htwg.codebreaker.view.gui.components.menu.playerActionMenu

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{VBox, HBox, BorderPane, Priority}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.TextAlignment

import de.htwg.codebreaker.model.player.laptop.RunningLaptopAction
import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.laptop.{CollectLaptopActionResultCommand, UpgradeCoresCommand}
import de.htwg.codebreaker.controller.commands.server.{StartRoleActionCommand, InstallServerRoleCommand, CollectRoleActionCommand}
import de.htwg.codebreaker.model.server.{Server, InstalledServerRole}
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.view.gui.components.menu.ObservableWindow

class LaptopMainMenu(
  protected val controller: ControllerInterface,
  playerIndex: Int
) extends ObservableWindow {

  override protected def createStage(): Stage = {
    val stage = super.createStage()
    stage.title = s"💻 Laptop"
    stage.width = 900
    stage.height = 600
    stage.resizable = true
    stage
  }

  override protected def refreshContent(): Unit = {
    currentStage.foreach { stage =>
      val player = controller.getPlayers(playerIndex)
      val currentRound = controller.game.state.round
      val runningCount = player.laptop.runningActions.count(_.completionRound > currentRound)
      val completedCount = controller.getCompletedActionsForCurrentPlayer()
        .count(_.completionRound <= currentRound)
      val currentCores = player.laptop.hardware.kerne
      val upgradeCost = UpgradeCoresCommand.calculateCost(currentCores)
      val canAffordUpgrade = player.laptop.hardware.cpu >= upgradeCost

      val header = new Label(s"${player.name} – Laptop") {
        style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10;"
        textAlignment = TextAlignment.Center
      }

      val leftColumn = createActionsColumn(player, runningCount, completedCount, currentCores, upgradeCost, canAffordUpgrade, stage)
      val rightColumn = createClaimedServersColumn()

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
    }
  }

  // ==========================================
  // LINKE SPALTE: AKTIONEN
  // ==========================================

  private def createActionsColumn(
      player: Player,
      runningCount: Int,
      completedCount: Int,
      currentCores: Int,
      upgradeCost: Int,
      canAffordUpgrade: Boolean,
      parentStage: Stage
    ): VBox = {
      
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
              },
              new Label(s"Cybersecurity: ${player.laptop.cybersecurity}%") {
                style = "-fx-text-fill: #ff6666; -fx-font-size: 11px;"
              },
              new Label(s"Netzwerkreichweite: ${player.laptop.hardware.networkRange}") {
                style = "-fx-text-fill: #ccc; -fx-font-size: 11px;"
              }
            )
          },
          new Label(s"Kerne: ${player.laptop.hardware.kerne}") {
            style = "-fx-text-fill: #ccc; -fx-font-size: 11px;"
          }
        )
      }

      val upgradeButton = new Button(s"⬆️ Upgrade Cores\n($currentCores → ${currentCores + 1}) [$upgradeCost CPU]") {
        style = if (canAffordUpgrade) 
          "-fx-font-size: 12px; -fx-padding: 10; -fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;"
        else
          "-fx-font-size: 12px; -fx-padding: 10; -fx-background-color: #555; -fx-text-fill: #999;"
        wrapText = true
        maxWidth = Double.MaxValue
        disable = !canAffordUpgrade
        onAction = _ => {
          controller.doAndRemember(UpgradeCoresCommand(playerIndex))
        }
      }

      val internetSearchButton = new Button("🌐 Internet durchsuchen") {
        style = "-fx-font-size: 13px; -fx-padding: 12; -fx-background-color: #1e90ff; -fx-text-fill: white; -fx-font-weight: bold;"
        maxWidth = Double.MaxValue
        onAction = _ => {
          new InternetSearchMenu(controller, playerIndex).show()
        }
      }

      val attackButton = new Button("🔨 Angriff") {
        style = "-fx-font-size: 13px; -fx-padding: 12; -fx-background-color: #7b68ee; -fx-text-fill: white; -fx-font-weight: bold;"
        maxWidth = Double.MaxValue
        onAction = _ => {
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
          upgradeButton,
          internetSearchButton,
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
    
    val inRangeServers = controller.getServers.filter { server =>
      val dx = math.abs(playerTile.x - server.tile.x)
      val dy = math.abs(playerTile.y - server.tile.y)
      math.max(dx, dy) <= player.laptop.hardware.networkRange
    }

    if (inRangeServers.isEmpty) {
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
      new LaptopActionSelectionMenu(controller, inRangeServers.head, playerIndex).show()
    } else {
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
  // RECHTE SPALTE: GECLAIMTE SERVER MIT ROLLEN
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
      val serverBoxes = claimedServers.map(createServerBoxWithRole)

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

  private def createServerBoxWithRole(s: Server): VBox = {
    val typeColor = s.serverType.toString match {
      case "Side" => "#66ff66"
      case "Firm" => "#66ccff"
      case "Cloud" => "#ffcc66"
      case "Bank" => "#ffb84d"
      case "Military" => "#ff6666"
      case "GKS" => "#ff99ff"
      case _ => "#888"
    }

    val serverInfoBox = new VBox {
      spacing = 3
      children = Seq(
        new Label(s"💻 ${s.name}") {
          style = s"-fx-text-fill: $typeColor; -fx-font-weight: bold; -fx-font-size: 12px;"
        },
        new Label(s"${s.serverType} | Diff: ${s.difficulty}%") {
          style = "-fx-text-fill: #ccc; -fx-font-size: 10px;"
        },
        new Label(s"Position: (${s.tile.x}, ${s.tile.y})") {
          style = "-fx-text-fill: #888; -fx-font-size: 9px;"
        }
      )
    }

    val roleSection = s.installedRole match {
      case None =>
        val installButton = new Button("⚙️ Install Role") {
          style = "-fx-font-size: 10px; -fx-padding: 5;"
          maxWidth = Double.MaxValue
          onAction = _ => showRoleSelectionWindow(s)
        }
        new VBox {
          spacing = 3
          children = Seq(
            new Label("No Role") {
              style = "-fx-text-fill: #888; -fx-font-size: 10px; -fx-font-style: italic;"
            },
            installButton
          )
        }

      case Some(role) if !role.isActive =>
        val currentRound = controller.game.state.round
        val roundsLeft = s.blockedUntilRound.getOrElse(currentRound) - currentRound
        
        if (roundsLeft <= 0) {
          val availableActions = controller.game.model.actionBlueprints
            .filter(_.roleType == role.roleType)
            .take(2)

          val actionButtons = availableActions.map { blueprint =>
            new Button(s"▶ ${blueprint.name}") {
              style = "-fx-font-size: 9px; -fx-padding: 3;"
              maxWidth = Double.MaxValue
              onAction = _ => showRoleManagementWindow(s, role)
            }
          }

          val moreActionsButton = new Button("⚙️ All Actions") {
            style = "-fx-font-size: 9px; -fx-padding: 3; -fx-background-color: #4db8ff; -fx-text-fill: white;"
            maxWidth = Double.MaxValue
            onAction = _ => showRoleManagementWindow(s, role)
          }

          new VBox {
            spacing = 3
            children = Seq(
              new Label(s"✅ ${role.roleType} Ready!") {
                style = "-fx-text-fill: #32cd32; -fx-font-size: 10px; -fx-font-weight: bold;"
              },
              new Label(s"Detection: ${role.detectionRisk}%") {
                style = s"-fx-text-fill: ${if (role.detectionRisk > 70) "#ff6666" else "#ffcc66"}; -fx-font-size: 9px;"
              }
            ) ++ actionButtons :+ moreActionsButton
          }
        } else {
          new VBox {
            spacing = 3
            children = Seq(
              new Label(s"⏳ Installing: ${role.roleType}") {
                style = "-fx-text-fill: #ff8c00; -fx-font-size: 10px; -fx-font-weight: bold;"
              },
              new Label(s"Ready in: $roundsLeft rounds") {
                style = "-fx-text-fill: #ff8c00; -fx-font-size: 9px;"
              }
            )
          }
        }

      case Some(role) =>
        val currentRound = controller.game.state.round
        val runningActions = role.runningActions.filter(_.completionRound > currentRound)
        val completedActions = role.runningActions.filter(_.completionRound <= currentRound)

        val manageButton = new Button("⚙️ Manage Role") {
          style = "-fx-font-size: 10px; -fx-padding: 5;"
          maxWidth = Double.MaxValue
          onAction = _ => showRoleManagementWindow(s, role)
        }

        new VBox {
          spacing = 3
          children = Seq(
            new Label(s"✓ ${role.roleType}") {
              style = "-fx-text-fill: #32cd32; -fx-font-size: 10px; -fx-font-weight: bold;"
            },
            new Label(s"Detection: ${role.detectionRisk}%") {
              style = s"-fx-text-fill: ${if (role.detectionRisk > 70) "#ff6666" else "#ffcc66"}; -fx-font-size: 9px;"
            },
            new Label(s"Range: ${role.networkRange}") {
              style = "-fx-text-fill: #66ccff; -fx-font-size: 9px;"
            },
            new Label(s"Running: ${runningActions.length} | Done: ${completedActions.length}") {
              style = "-fx-text-fill: #ccc; -fx-font-size: 9px;"
            },
            manageButton
          )
        }
    }

    new VBox {
      style = s"-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: $typeColor; -fx-border-width: 2;"
      spacing = 8
      children = Seq(serverInfoBox, roleSection)
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
        new Label(s"Fertig in Runde: ${action.completionRound} (${roundsLeft} Runden übrig)") {
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

  // ==========================================
  // ROLE SELECTION WINDOW
  // ==========================================

  private def showRoleSelectionWindow(server: Server): Unit = {
    val stage = new Stage {
      title = s"Install Role: ${server.name}"
      width = 500
      height = 400
      resizable = false
    }

    val availableRoles = controller.game.model.roleBlueprints

    val roleButtons = availableRoles.map { blueprint =>
      val setupText = s"Setup: ${blueprint.setupDurationRounds} rounds"
      val riskText = s"Risk: ${blueprint.baseDetectionRisk}%"
      
      new Button(s"${blueprint.name}\n$setupText | $riskText\n${blueprint.description}") {
        style = "-fx-font-size: 11px; -fx-padding: 10; -fx-wrap-text: true;"
        wrapText = true
        maxWidth = Double.MaxValue
        prefHeight = 80
        onAction = _ => {
          controller.doAndRemember(
            InstallServerRoleCommand(playerIndex, server.name, blueprint.roleType)
          )
          stage.close()
        }
      }
    }

    val scrollPane = new ScrollPane {
      content = new VBox {
        spacing = 8
        padding = Insets(10)
        children = roleButtons
      }
      fitToWidth = true
    }

    val cancelButton = new Button("❌ Cancel") {
      style = "-fx-font-size: 12px; -fx-padding: 10;"
      maxWidth = Double.MaxValue
      onAction = _ => stage.close()
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 10
        padding = Insets(10)
        children = Seq(
          new Label(s"Select Role for ${server.name}") {
            style = "-fx-font-size: 14px; -fx-font-weight: bold;"
          },
          scrollPane,
          cancelButton
        )
      }
    )

    stage.show()
  }

  // ==========================================
  // ROLE MANAGEMENT WINDOW
  // ==========================================

  private def showRoleManagementWindow(server: Server, role: InstalledServerRole): Unit = {
    val stage = new Stage {
      title = s"${server.name} - ${role.roleType}"
      width = 700
      height = 500
      resizable = true
    }

    val currentRound = controller.game.state.round

    val header = new VBox {
      spacing = 5
      padding = Insets(10)
      style = "-fx-background-color: #2a2a2a;"
      children = Seq(
        new Label(s"💻 ${server.name} - ${role.roleType}") {
          style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4db8ff;"
        },
        new Label(s"Detection Risk: ${role.detectionRisk}%") {
          val color = if (role.detectionRisk > 70) "#ff6666" else "#ffcc66"
          style = s"-fx-font-size: 12px; -fx-text-fill: $color;"
        }
      )
    }

    val runningActions = role.runningActions.filter(_.completionRound > currentRound)
    val runningSection = if (runningActions.isEmpty) {
      new VBox {
        spacing = 5
        children = Seq(
          new Label("No running actions") {
            style = "-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-style: italic;"
          }
        )
      }
    } else {
      new VBox {
        spacing = 5
        children = runningActions.map { action =>
          val roundsLeft = action.completionRound - currentRound
          new Label(s"⏳ ${action.actionId} - ${roundsLeft} rounds left") {
            style = "-fx-font-size: 11px; -fx-text-fill: #ff8c00;"
          }
        }
      }
    }

    val completedActions = role.runningActions.filter(_.completionRound <= currentRound)
    val completedSection = if (completedActions.isEmpty) {
      new VBox {
        spacing = 5
        children = Seq(
          new Label("No completed actions") {
            style = "-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-style: italic;"
          }
        )
      }
    } else {
      new VBox {
        spacing = 5
        children = completedActions.map { action =>
          val rewards = action.expectedRewards
          
          new HBox {
            spacing = 10
            alignment = Pos.CenterLeft
            children = Seq(
              new VBox {
                spacing = 3
                children = Seq(
                  new Label(s"✓ ${action.actionId}") {
                    style = "-fx-font-size: 11px; -fx-text-fill: #32cd32; -fx-font-weight: bold;"
                  },
                  new Label(s"Rewards: CPU +${rewards.cpu} | RAM +${rewards.ram} | Code +${rewards.code}") {
                    style = "-fx-font-size: 9px; -fx-text-fill: #888;"
                  }
                )
              },
              new Button("📦 Collect") {
                style = "-fx-font-size: 10px; -fx-padding: 5;"
                onAction = _ => {
                  controller.doAndRemember(
                    CollectRoleActionCommand(playerIndex, server.name, action.actionId)
                  )
                  stage.close()
                  
                  val updatedServer = controller.getServers.find(_.name == server.name)
                  updatedServer.flatMap(_.installedRole).foreach { updatedRole =>
                    val wasDetected = updatedRole.detectionRisk > role.detectionRisk
                    if (wasDetected) {
                      showDetectedNotification(server.name)
                    } else {
                      showRewardsNotification(rewards.cpu, rewards.ram, rewards.code)
                    }
                  }
                }
              }
            )
          }
        }
      }
    }

    val availableActions = controller.game.model.actionBlueprints
      .filter(_.roleType == role.roleType)

    val actionButtons = availableActions.map { blueprint =>
      val durationText = s"Duration: ${blueprint.durationRounds} rounds"
      val riskText = s"Risk: +${blueprint.detectionRiskIncrease}%"
      val rewardText = s"Rewards: CPU +${blueprint.rewards.cpu} | RAM +${blueprint.rewards.ram} | Code +${blueprint.rewards.code}"
      
      new Button(s"▶ ${blueprint.name}\n$durationText | $riskText\n$rewardText\n${blueprint.description}") {
        style = "-fx-font-size: 11px; -fx-padding: 10; -fx-wrap-text: true;"
        wrapText = true
        maxWidth = Double.MaxValue
        prefHeight = 100
        onAction = _ => {
          controller.doAndRemember(
            StartRoleActionCommand(playerIndex, server.name, blueprint.id)
          )
          stage.close()
        }
      }
    }

    val scrollPane = new ScrollPane {
      content = new VBox {
        spacing = 15
        padding = Insets(10)
        children = Seq(
          new Label("═══ Running Actions ═══") {
            style = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ff8c00;"
          },
          runningSection,
          new Label("═══ Completed Actions ═══") {
            style = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #32cd32;"
          },
          completedSection,
          new Label("═══ Start New Action ═══") {
            style = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4db8ff;"
          }
        ) ++ actionButtons
      }
      fitToWidth = true
    }

    val closeButton = new Button("❌ Close") {
      style = "-fx-font-size: 12px; -fx-padding: 10;"
      maxWidth = Double.MaxValue
      onAction = _ => stage.close()
    }

    stage.scene = new Scene(
      new BorderPane {
        top = header
        center = scrollPane
        bottom = closeButton
      }
    )

    stage.show()
  }

  // ==========================================
  // NOTIFICATION WINDOWS
  // ==========================================

  private def showRewardsNotification(cpu: Int, ram: Int, code: Int): Unit = {
    val stage = new Stage {
      title = "✅ Erfolgreich!"
      width = 400
      height = 250
      resizable = false
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 15
        padding = Insets(20)
        alignment = Pos.Center
        style = "-fx-background-color: #1a1a1a;"
        children = Seq(
          new Label("✅ Rewards eingesammelt!") {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #32cd32;"
          },
          new VBox {
            spacing = 8
            alignment = Pos.Center
            style = "-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-border-radius: 5; -fx-background-radius: 5;"
            children = Seq(
              if (cpu > 0) Some(new Label(s"💚 +$cpu CPU") {
                style = "-fx-font-size: 14px; -fx-text-fill: #66ff66;"
              }) else None,
              if (ram > 0) Some(new Label(s"💙 +$ram RAM") {
                style = "-fx-font-size: 14px; -fx-text-fill: #66ccff;"
              }) else None,
              if (code > 0) Some(new Label(s"💛 +$code Code") {
                style = "-fx-font-size: 14px; -fx-text-fill: #ffcc66;"
              }) else None
            ).flatten
          },
          new Button("OK") {
            style = "-fx-font-size: 13px; -fx-padding: 10; -fx-background-color: #4caf50; -fx-text-fill: white;"
            maxWidth = 200
            onAction = _ => stage.close()
          }
        )
      }
    )

    stage.show()
  }

  private def showDetectedNotification(serverName: String): Unit = {
    val stage = new Stage {
      title = "⚠️ Entdeckt!"
      width = 450
      height = 250
      resizable = false
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 15
        padding = Insets(20)
        alignment = Pos.Center
        style = "-fx-background-color: #1a1a1a;"
        children = Seq(
          new Label("⚠️ ENTDECKT!") {
            style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ff6666;"
          },
          new Label(s"Deine Action auf $serverName wurde entdeckt!") {
            style = "-fx-font-size: 14px; -fx-text-fill: #ccc;"
            wrapText = true
          },
          new VBox {
            spacing = 5
            alignment = Pos.Center
            style = "-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-border-radius: 5; -fx-background-radius: 5;"
            children = Seq(
              new Label("Konsequenzen:") {
                style = "-fx-font-size: 12px; -fx-text-fill: #ff8c00; -fx-font-weight: bold;"
              },
              new Label("• Detection Risk +20%") {
                style = "-fx-font-size: 11px; -fx-text-fill: #ff6666;"
              },
              new Label("• Server 3 Runden geblockt") {
                style = "-fx-font-size: 11px; -fx-text-fill: #ff6666;"
              },
              new Label("• Keine Rewards erhalten") {
                style = "-fx-font-size: 11px; -fx-text-fill: #ff6666;"
              }
            )
          },
          new Button("OK") {
            style = "-fx-font-size: 13px; -fx-padding: 10; -fx-background-color: #dc143c; -fx-text-fill: white;"
            maxWidth = 200
            onAction = _ => stage.close()
          }
        )
      }
    )

    stage.show()
  }
}
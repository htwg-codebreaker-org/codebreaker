// src/main/scala/de/htwg/codebreaker/view/gui/components/menu/playerActionMenu/InternetSearchMenu.scala
package de.htwg.codebreaker.view.gui.components.menu.playerActionMenu

import scalafx.scene.Scene
import scalafx.Includes.jfxBooleanProperty2sfx
import scalafx.scene.control.{Button, Label, CheckBox, Alert}
import scalafx.scene.layout.{VBox, HBox, BorderPane}
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scala.collection.mutable

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.laptop._
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.RunningInternetSearch

class InternetSearchMenu(
  controller: ControllerInterface,
  playerIndex: Int
) {

  def show(): Unit = {
    val stage = new Stage {
      title = "🌐 Internet durchsuchen"
      width = 700
      height = 500
      resizable = true
    }

    val player = controller.getPlayers(playerIndex)
    val currentRound = controller.game.state.round

    val content = player.laptop.runningInternetSearch match {
      case None =>
        // Keine Suche gestartet
        createStartSearchView(player, stage)
      
      case Some(search) if search.completionRound > currentRound =>
        // Suche läuft noch
        createRunningSearchView(search, currentRound, stage)
      
      case Some(search) =>
        // Suche abgeschlossen - Ergebnisse anzeigen
        createResultsView(search, stage)
    }

    stage.scene = new Scene(
      new BorderPane {
        center = content
      }
    )

    stage.show()
  }

  // ==========================================
  // START SEARCH VIEW
  // ==========================================

  private def createStartSearchView(player: Player, parentStage: Stage): VBox = {
    val codeCost = 20
    val canAfford = player.laptop.hardware.code >= codeCost

    new VBox {
      spacing = 20
      padding = Insets(30)
      alignment = Pos.Center
      style = "-fx-background-color: #1a1a1a;"
      children = Seq(
        new Label("🌐 Internet durchsuchen") {
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4db8ff;"
        },
        new Label("Durchsuche das Darknet nach neuen Hacking-Tools") {
          style = "-fx-font-size: 14px; -fx-text-fill: #ccc;"
          wrapText = true
        },
        new VBox {
          spacing = 8
          alignment = Pos.Center
          style = "-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-border-radius: 5; -fx-background-radius: 5;"
          children = Seq(
            new Label("💰 Kosten: 20 Code") {
              style = "-fx-font-size: 13px; -fx-text-fill: #ffcc66;"
            },
            new Label("⏱️ Dauer: 2 Runden") {
              style = "-fx-font-size: 13px; -fx-text-fill: #66ccff;"
            },
            new Label("🎁 Belohnung: 1-3 zufällige Tools") {
              style = "-fx-font-size: 13px; -fx-text-fill: #66ff66;"
            }
          )
        },
        new Label(s"Dein Code: ${player.laptop.hardware.code}") {
          style = if (canAfford) 
            "-fx-font-size: 14px; -fx-text-fill: #32cd32; -fx-font-weight: bold;"
          else
            "-fx-font-size: 14px; -fx-text-fill: #ff6666; -fx-font-weight: bold;"
        },
        new Button("🔍 Suche starten") {
          style = if (canAfford)
            "-fx-font-size: 15px; -fx-padding: 15; -fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;"
          else
            "-fx-font-size: 15px; -fx-padding: 15; -fx-background-color: #555; -fx-text-fill: #999;"
          disable = !canAfford
          maxWidth = 300
          onAction = _ => {
            controller.doAndRemember(SearchInternetCommand(playerIndex))
            parentStage.close()
            show() // Refresh
          }
        },
        new Button("❌ Abbrechen") {
          style = "-fx-font-size: 13px; -fx-padding: 10; -fx-background-color: #dc143c; -fx-text-fill: white;"
          maxWidth = 200
          onAction = _ => parentStage.close()
        }
      )
    }
  }

  // ==========================================
  // RUNNING SEARCH VIEW
  // ==========================================

  private def createRunningSearchView(
    search: RunningInternetSearch,
    currentRound: Int,
    parentStage: Stage
  ): VBox = {
    val roundsLeft = search.completionRound - currentRound

    new VBox {
      spacing = 20
      padding = Insets(30)
      alignment = Pos.Center
      style = "-fx-background-color: #1a1a1a;"
      children = Seq(
        new Label("🔍 Suche läuft...") {
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ff8c00;"
        },
        new Label("Durchsuche versteckte Darknet-Foren und Hacker-Communities...") {
          style = "-fx-font-size: 14px; -fx-text-fill: #ccc;"
          wrapText = true
        },
        new VBox {
          spacing = 10
          alignment = Pos.Center
          style = "-fx-background-color: #2a2a2a; -fx-padding: 20; -fx-border-radius: 5; -fx-background-radius: 5;"
          children = Seq(
            new Label(s"⏳ Noch $roundsLeft Runden") {
              style = "-fx-font-size: 18px; -fx-text-fill: #ff8c00; -fx-font-weight: bold;"
            },
            new Label(s"Fertig in Runde: ${search.completionRound}") {
              style = "-fx-font-size: 13px; -fx-text-fill: #888;"
            }
          )
        },
        new Button("OK") {
          style = "-fx-font-size: 13px; -fx-padding: 10;"
          maxWidth = 200
          onAction = _ => parentStage.close()
        }
      )
    }
  }

  // ==========================================
  // RESULTS VIEW
  // ==========================================

  private def createResultsView(
    search: RunningInternetSearch,
    parentStage: Stage
  ): VBox = {
    val selectedTools = mutable.Set[String]()

    val toolCheckBoxes = search.foundTools.map { tool =>
      val checkbox = new CheckBox(tool.name) {
        selected = true
        style = "-fx-font-size: 14px; -fx-text-fill: white;"
      }
      selectedTools.add(tool.id)
      
      checkbox.selectedProperty().onChange { (_, _, isSelected) =>
        if (isSelected) selectedTools.add(tool.id)
        else selectedTools.remove(tool.id)
      }

      new VBox {
        spacing = 5
        style = "-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #4db8ff; -fx-border-width: 2;"
        children = Seq(
          checkbox,
          new Label(tool.description) {
            style = "-fx-font-size: 11px; -fx-text-fill: #ccc;"
            wrapText = true
          },
          new HBox {
            spacing = 10
            children = Seq(
              new Label(s"Hack: +${tool.hackBonus}%") {
                style = "-fx-font-size: 10px; -fx-text-fill: #66ff66;"
              },
              new Label(s"Stealth: +${tool.stealthBonus}%") {
                style = "-fx-font-size: 10px; -fx-text-fill: #66ccff;"
              },
              new Label(s"Actions: ${tool.availableActions.length}") {
                style = "-fx-font-size: 10px; -fx-text-fill: #ffcc66;"
              }
            )
          }
        )
      }
    }

    new VBox {
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: #1a1a1a;"
      children = Seq(
        new Label("🎉 Suche abgeschlossen!") {
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #32cd32;"
        },
        new Label(s"${search.foundTools.length} Tool(s) gefunden:") {
          style = "-fx-font-size: 14px; -fx-text-fill: #ccc;"
        }
      ) ++ toolCheckBoxes ++ Seq(
        new HBox {
          spacing = 15
          alignment = Pos.Center
          padding = Insets(10, 0, 0, 0)
          children = Seq(
            new Button("✓ Ausgewählte installieren") {
              style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = _ => {
                controller.doAndRemember(
                  CollectInternetSearchCommand(playerIndex, selectedTools.toList)
                )
                parentStage.close()
                
                if (selectedTools.nonEmpty) {
                  val successAlert = new Alert(AlertType.Information) {
                    title = "Tools installiert"
                    headerText = s"${selectedTools.size} Tool(s) erfolgreich installiert!"
                  }
                  successAlert.showAndWait()
                }
              }
            },
            new Button("❌ Alle verwerfen") {
              style = "-fx-font-size: 13px; -fx-padding: 10; -fx-background-color: #dc143c; -fx-text-fill: white;"
              onAction = _ => {
                controller.doAndRemember(
                  CollectInternetSearchCommand(playerIndex, Nil)
                )
                parentStage.close()
              }
            }
          )
        }
      )
    }
  }
}
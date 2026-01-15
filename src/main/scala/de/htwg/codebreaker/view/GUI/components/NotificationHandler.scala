package de.htwg.codebreaker.view.gui.components

import de.htwg.codebreaker.model.{Server, Player}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.application.Platform

/**
 * Verwaltet Benachrichtigungen und Popups.
 */
class NotificationHandler {
  
  /**
   * Zeigt eine Benachrichtigung für einen erfolgreich gehackten Server an.
   */
  def showHackSuccessNotification(
    server: Server,
    oldPlayer: Player,
    newPlayer: Player
  ): Unit = {
    val rewards = calculateRewards(oldPlayer, newPlayer)
    val rewardText = formatRewards(rewards)
    
    Platform.runLater {
      val alert = new Alert(AlertType.Information) {
        title = "Server gehackt!"
        headerText = s"Server '${server.name}' erfolgreich gehackt!"
        contentText = s"""Server-Typ: ${server.serverType}
                         |Schwierigkeit: ${server.difficulty}%
                         |
                         |Belohnungen erhalten:
                         |$rewardText""".stripMargin
      }
      alert.showAndWait()
    }
  }
  
  /**
   * Zeigt eine Benachrichtigung für einen gehackten Server (ohne Spieler-Details).
   */
  def showHackSuccessNotificationSimple(server: Server): Unit = {
    Platform.runLater {
      val alert = new Alert(AlertType.Information) {
        title = "Server gehackt!"
        headerText = s"Server '${server.name}' erfolgreich gehackt!"
        contentText = s"""Server-Typ: ${server.serverType}
                         |Schwierigkeit: ${server.difficulty}%
                         |
                         |Belohnungen erhalten!""".stripMargin
      }
      alert.showAndWait()
    }
  }
  
  /**
   * Berechnet die erhaltenen Belohnungen basierend auf Spieler-Änderungen.
   */
  private def calculateRewards(oldPlayer: Player, newPlayer: Player): Map[String, Int] = {
    Map(
      "CPU" -> (newPlayer.cpu - oldPlayer.cpu),
      "RAM" -> (newPlayer.ram - oldPlayer.ram),
      "Code" -> (newPlayer.code - oldPlayer.code),
      "XP" -> (newPlayer.xp - oldPlayer.xp)
    ).filter(_._2 > 0)
  }
  
  /**
   * Formatiert die Belohnungen als Text.
   */
  private def formatRewards(rewards: Map[String, Int]): String = {
    if (rewards.isEmpty) {
      "Keine Belohnungen"
    } else {
      rewards.map { case (resource, amount) => s"+$amount $resource" }.mkString("\n")
    }
  }
  
  /**
   * Erkennt neu gehackte Server und zeigt Benachrichtigungen an.
   */
  def detectAndShowHackResults(
    oldServers: List[Server],
    newServers: List[Server],
    oldPlayers: List[Player],
    newPlayers: List[Player]
  ): Unit = {
    val newlyHackedServers = newServers.filter { newServer =>
      newServer.hacked && oldServers.exists(oldServer =>
        oldServer.name == newServer.name && !oldServer.hacked
      )
    }
    
    newlyHackedServers.foreach { server =>
      server.hackedBy.foreach { playerIndex =>
        (oldPlayers.lift(playerIndex), newPlayers.lift(playerIndex)) match {
          case (Some(oldPlayer), Some(newPlayer)) =>
            showHackSuccessNotification(server, oldPlayer, newPlayer)
          case _ =>
            showHackSuccessNotificationSimple(server)
        }
      }
    }
  }
}
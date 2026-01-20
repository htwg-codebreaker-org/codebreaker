package de.htwg.codebreaker.controller.commands.laptop

import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.player.Player

/**
 * Upgrade für Laptop-Kerne.
 * Kosten steigen quadratisch: BASE_COST * (aktuelle Kerne)²
 */
case class UpgradeCoresCommand(playerIndex: Int) extends Command {

  private var previousPlayerState: Option[Player] = None

  override def doStep(game: Game): Try[Game] = {
    if (playerIndex < 0 || playerIndex >= game.model.players.length)
      return Failure(new IllegalArgumentException("Ungültiger Spieler"))

    val player = game.model.players(playerIndex)
    previousPlayerState = Some(player)

    val currentCores = player.laptop.hardware.kerne
    val upgradeCost = UpgradeCoresCommand.calculateCost(currentCores)

    // Prüfen ob genug CPU vorhanden
    if (player.laptop.hardware.cpu < upgradeCost)
      return Failure(new IllegalArgumentException(
        s"Nicht genug CPU! Benötigt: $upgradeCost, Verfügbar: ${player.laptop.hardware.cpu}"
      ))

    // CPU abziehen, Kerne erhöhen
    val updatedHardware = player.laptop.hardware.copy(
      cpu = player.laptop.hardware.cpu - upgradeCost,
      kerne = currentCores + 1
    )

    val updatedLaptop = player.laptop.copy(hardware = updatedHardware)
    val updatedPlayer = player.copy(laptop = updatedLaptop)

    Success(game.copy(
      model = game.model.copy(
        players = game.model.players.updated(playerIndex, updatedPlayer)
      )
    ))
  }

  override def undoStep(game: Game): Try[Game] = Try {
    previousPlayerState match {
      case Some(oldPlayer) =>
        game.copy(
          model = game.model.copy(
            players = game.model.players.updated(playerIndex, oldPlayer)
          )
        )
      case None => game
    }
  }
}

object UpgradeCoresCommand {
  val BASE_COST = 100
  
  def calculateCost(currentCores: Int): Int = BASE_COST * currentCores * currentCores
}
package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.Server
import de.htwg.codebreaker.model.game.Game

import scala.util.{Success, Try}

/** Command zum Claimen eines Servers durch einen Spieler.
  */
case class ClaimServerCommand(serverName: String, playerIndex: Int) extends Command {
  override def doStep(game: Game): Try[Game] = Try {
    val updatedServers = game.model.servers.map {
      case s if s.name == serverName => Server.claim(s, playerIndex)
      case s                         => s
    }
    val updatedModel   = game.model.copy(servers = updatedServers)
    game.copy(model = updatedModel)
  }

  override def undoStep(game: Game): Try[Game] = Try {
    val updatedServers = game.model.servers.map {
      case s if s.name == serverName => Server.unclaim(s)
      case s                         => s
    }
    val updatedModel   = game.model.copy(servers = updatedServers)
    game.copy(model = updatedModel)
  }
}

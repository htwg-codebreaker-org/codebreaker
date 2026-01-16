package de.htwg.codebreaker.controller.commands

import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.model.{Player, HackSkill, PlayerSkillTree}
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.controller._

/**
 * Command zum Freischalten eines Skills für einen Spieler.
 *
 * Regeln:
 * - Spieler-Index muss gültig sein
 * - Skill darf noch nicht freigeschaltet sein
 * - Spieler muss genug availableXp haben
 *
 * @param playerIndex Index des Spielers
 * @param skill Skill, der freigeschaltet werden soll
 */
case class UnlockSkillCommand(
  playerIndex: Int,
  skill: HackSkill
) extends Command {

  private var previousPlayerState: Option[Player] = None

  override def doStep(game: Game): Try[Game] = {
    val players = game.model.players

    // === Spieler-Index prüfen ===
    if (playerIndex < 0 || playerIndex >= players.length) {
      return Failure(
        new IllegalArgumentException(s"Ungültiger Spieler-Index: $playerIndex")
      )
    }

    val player = players(playerIndex)

    // === Skill bereits freigeschaltet? ===
    if (player.skills.unlockedSkillIds.contains(skill.id)) {
      return Failure(
        new IllegalArgumentException(
          s"Skill '${skill.name}' ist bereits freigeschaltet"
        )
      )
    }

    // === Genug XP? ===
    if (player.availableXp < skill.costXp) {
      return Failure(
        new IllegalArgumentException(
          s"Nicht genug XP (benötigt: ${skill.costXp}, vorhanden: ${player.availableXp})"
        )
      )
    }

    // === Undo-State speichern ===
    previousPlayerState = Some(player)

    // === Player aktualisieren ===
    val updatedPlayer = player.copy(
      availableXp = player.availableXp - skill.costXp,
      skills = PlayerSkillTree(
        unlockedSkillIds = player.skills.unlockedSkillIds + skill.id
      )
    )

    val updatedPlayers =
      players.updated(playerIndex, updatedPlayer)

    Success(
      game.copy(
        model = game.model.copy(players = updatedPlayers)
      )
    )
  }

  override def undoStep(game: Game): Try[Game] = Try {
    previousPlayerState match {
      case Some(oldPlayer) =>
        val players = game.model.players
        val revertedPlayers =
          players.updated(playerIndex, oldPlayer)

        game.copy(
          model = game.model.copy(players = revertedPlayers)
        )

      case None =>
        throw new IllegalStateException(
          "Kein vorheriger Spieler-Zustand für Undo gespeichert"
        )
    }
  }
}

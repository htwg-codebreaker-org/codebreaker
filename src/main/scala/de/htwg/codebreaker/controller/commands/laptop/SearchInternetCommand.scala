// src/main/scala/de/htwg/codebreaker/controller/commands/laptop/SearchInternetCommand.scala
package de.htwg.codebreaker.controller.commands.laptop

import scala.util.{Try, Success, Failure, Random}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.{LaptopTool, RunningInternetSearch}

/**
 * Command zum STARTEN der Internet-Suche.
 * Läuft 2 Runden, findet dann 1-3 Tools.
 */
case class SearchInternetCommand(
  playerIndex: Int,
  codeCost: Int = 20,
  durationRounds: Int = 2,
  random: Random = Random()
) extends Command {

  private var previousPlayerState: Option[Player] = None

  override def doStep(game: Game): Try[Game] = {
    if (playerIndex < 0 || playerIndex >= game.model.players.length)
      return Failure(new IllegalArgumentException("Ungültiger Spieler"))

    val player = game.model.players(playerIndex)
    previousPlayerState = Some(player)

    // Prüfe ob bereits eine Suche läuft
    if (player.laptop.runningInternetSearch.nonEmpty)
      return Failure(new IllegalArgumentException("Es läuft bereits eine Internet-Suche"))

    // Ressourcen prüfen
    if (player.laptop.hardware.code < codeCost)
      return Failure(new IllegalArgumentException(s"Nicht genug Code (benötigt: $codeCost)"))

    // Finde Tools die der Spieler noch nicht hat
    val availableTools = game.model.laptopTools
      .filterNot(tool => player.laptop.tools.hasTool(tool.id))

    if (availableTools.isEmpty)
      return Failure(new IllegalArgumentException("Du hast bereits alle Tools!"))

    // Wähle zufällig 1-3 Tools aus
    val numTools = math.min(1 + random.nextInt(3), availableTools.length)
    val foundTools = random.shuffle(availableTools).take(numTools)

    // Code abziehen
    val updatedHardware = player.laptop.hardware.copy(
      code = player.laptop.hardware.code - codeCost
    )

    // Running Search erstellen
    val runningSearch = RunningInternetSearch(
      startRound = game.state.round,
      completionRound = game.state.round + durationRounds,
      foundTools = foundTools
    )

    val updatedLaptop = player.laptop.copy(
      hardware = updatedHardware,
      runningInternetSearch = Some(runningSearch)
    )

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

/**
 * Command zum ABHOLEN der gefundenen Tools.
 * Installiert alle ausgewählten Tools.
 */
case class CollectInternetSearchCommand(
  playerIndex: Int,
  selectedToolIds: List[String]
) extends Command {

  private var previousPlayerState: Option[Player] = None

  override def doStep(game: Game): Try[Game] = {
    val player = game.model.players(playerIndex)
    previousPlayerState = Some(player)

    player.laptop.runningInternetSearch match {
      case None =>
        Failure(new IllegalArgumentException("Keine laufende Internet-Suche"))
      
      case Some(search) if search.completionRound > game.state.round =>
        Failure(new IllegalArgumentException("Suche noch nicht abgeschlossen"))
      
      case Some(search) =>
        // Validiere ausgewählte Tools
        val validTools = search.foundTools.filter(tool => 
          selectedToolIds.contains(tool.id)
        )

        // Installiere ausgewählte Tools (kann auch leer sein = alle verwerfen)
        val updatedTools = if (validTools.nonEmpty) {
          player.laptop.tools.copy(
            installedTools = validTools ::: player.laptop.tools.installedTools
          )
        } else {
          player.laptop.tools
        }

        val updatedLaptop = player.laptop.copy(
          tools = updatedTools,
          runningInternetSearch = None  // Suche abschließen
        )

        // XP Belohnung: 10 XP pro installiertem Tool
        val xpGained = validTools.length * 10

        val updatedPlayer = player.copy(
          laptop = updatedLaptop,
          availableXp = player.availableXp + xpGained,
          totalXpEarned = player.totalXpEarned + xpGained
        )

        Success(game.copy(
          model = game.model.copy(
            players = game.model.players.updated(playerIndex, updatedPlayer)
          )
        ))
    }
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
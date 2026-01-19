package de.htwg.codebreaker.controller.commands

import scala.util.{Try, Success, Failure, Random}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.{RunningLaptopAction, LaptopAction}
import de.htwg.codebreaker.model.server.Server

// ==================== SCHRITT 1: Action starten ====================

/**
 * Startet eine Laptop-Action (z.B. BruteForce).
 * Die Action läuft dann über mehrere Runden.
 */
case class StartLaptopActionCommand(
  playerIndex: Int,
  action: LaptopAction,
  targetServerName: String
) extends Command {

  private var previousPlayerState: Option[Player] = None

  override def doStep(game: Game): Try[Game] = {
    // Spieler validieren
    if (playerIndex < 0 || playerIndex >= game.model.players.length)
      return Failure(new IllegalArgumentException("Ungültiger Spieler"))

    val player = game.model.players(playerIndex)
    previousPlayerState = Some(player)

    // Server finden
    val serverOpt = game.model.servers.find(_.name == targetServerName)
    if (serverOpt.isEmpty)
      return Failure(new IllegalArgumentException("Server nicht gefunden"))
    
    val server = serverOpt.get

    // Validierungen
    if (player.tile != server.tile)
      return Failure(new IllegalArgumentException("Spieler nicht auf Server-Tile"))

    if (server.hacked)
      return Failure(new IllegalArgumentException("Server bereits gehackt"))

    // Ressourcen prüfen
    if (player.laptop.hardware.cpu < action.cpuCost)
      return Failure(new IllegalArgumentException("Nicht genug CPU"))
    
    if (player.laptop.hardware.ram < action.ramCost)
      return Failure(new IllegalArgumentException("Nicht genug RAM"))

    if (player.laptop.hardware.kerne < action.coreCost)
      return Failure(new IllegalArgumentException("Nicht genug Kerne verfügbar"))

    // Ressourcen abziehen
    val updatedHardware = player.laptop.hardware.copy(
      cpu = player.laptop.hardware.cpu - action.cpuCost,
      ram = player.laptop.hardware.ram - action.ramCost,
      kerne = player.laptop.hardware.kerne - action.coreCost
    )

    // Running Action erstellen
    val runningAction = RunningLaptopAction(
      action = action,
      startRound = game.state.round,
      completionRound = game.state.round + action.durationRounds,
      targetServer = targetServerName
    )

    // Zum Laptop hinzufügen
    val updatedLaptop = player.laptop.copy(
      hardware = updatedHardware,
      runningActions = runningAction :: player.laptop.runningActions
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

// ==================== SCHRITT 2: Actions verarbeiten (Placeholder) ====================

/**
 * Wird automatisch bei NextPlayerCommand aufgerufen.
 * Macht momentan nichts - Actions bleiben bis sie mit CollectLaptopActionResultCommand abgeholt werden.
 * Kerne werden erst freigegeben wenn man die Belohnung abholt!
 */
case class ProcessLaptopActionsCommand(currentRound: Int) extends Command {

  override def doStep(game: Game): Try[Game] = {
    // Actions werden NICHT gelöscht - bleiben bis CollectLaptopActionResultCommand sie abholt
    Success(game)
  }

  override def undoStep(game: Game): Try[Game] = Try {
    game
  }
}

// ==================== SCHRITT 3: Ergebnis abholen ====================

/**
 * Holt das Ergebnis einer fertigen Action ab.
 * Spieler kann dann wählen: Claimen oder Daten klauen.
 */
case class CollectLaptopActionResultCommand(
  playerIndex: Int,
  targetServerName: String,
  claimServer: Boolean,  // true = Server claimen, false = nur Daten klauen
  random: Random = Random()
) extends Command {

  private var previousPlayerState: Option[Player] = None
  private var previousServerState: Option[Server] = None

  override def doStep(game: Game): Try[Game] = {
    val player = game.model.players(playerIndex)
    previousPlayerState = Some(player)

    // Finde die fertige Action für diesen Server
    val completedActionOpt = player.laptop.runningActions.find { running =>
      running.targetServer == targetServerName && 
      running.completionRound <= game.state.round
    }

    if (completedActionOpt.isEmpty)
      return Failure(new IllegalArgumentException("Keine fertige Action für diesen Server"))

    val completedAction = completedActionOpt.get
    val server = game.model.servers.find(_.name == targetServerName).get
    previousServerState = Some(server)

    // Erfolgschance berechnen
    val baseChance = 100 - server.difficulty
    val securityBonus = player.cybersecurity / 2
    val successChance = math.max(5, math.min(95, baseChance + securityBonus))

    val success = random.nextInt(100) < successChance

    if (!success) {
      // Fehlgeschlagen - Action entfernen UND Kerne freigeben
      val releasedCores = completedAction.action.coreCost
      val updatedLaptop = player.laptop.copy(
        runningActions = player.laptop.runningActions.filterNot(_.targetServer == targetServerName),
        hardware = player.laptop.hardware.copy(
          kerne = player.laptop.hardware.kerne + releasedCores
        )
      )
      val updatedPlayer = player.copy(laptop = updatedLaptop)

      return Success(game.copy(
        model = game.model.copy(
          players = game.model.players.updated(playerIndex, updatedPlayer)
        )
      ))
    }

    // Erfolgreich! Rewards berechnen
    val rewards = calculateRewards(server, claimServer)

    // Action aus runningActions entfernen + Kerne freigeben
    val releasedCores = completedAction.action.coreCost
    val updatedLaptop = player.laptop.copy(
      runningActions = player.laptop.runningActions.filterNot(_.targetServer == targetServerName),
      hardware = player.laptop.hardware.copy(
        cpu = player.laptop.hardware.cpu + rewards.cpuGained,
        ram = player.laptop.hardware.ram + rewards.ramGained,
        code = player.laptop.hardware.code + rewards.codeGained,
        kerne = player.laptop.hardware.kerne + releasedCores
      )
    )

    val updatedPlayer = player.copy(
      laptop = updatedLaptop,
      availableXp = player.availableXp + rewards.xpGained,
      totalXpEarned = player.totalXpEarned + rewards.xpGained
    )

    // Server updaten (nur wenn geclaimed)
    val updatedServer = if (claimServer) {
      server.copy(
        hacked = true,
        hackedBy = Some(playerIndex),
        claimedBy = Some(playerIndex)
      )
    } else {
      server.copy(
        hacked = true,
        hackedBy = Some(playerIndex)
      )
    }

    val updatedServers = game.model.servers.map {
      case s if s.name == targetServerName => updatedServer
      case s => s
    }

    Success(game.copy(
      model = game.model.copy(
        players = game.model.players.updated(playerIndex, updatedPlayer),
        servers = updatedServers
      )
    ))
  }

  override def undoStep(game: Game): Try[Game] = Try {
    val players = previousPlayerState match {
      case Some(oldPlayer) => game.model.players.updated(playerIndex, oldPlayer)
      case None => game.model.players
    }

    val servers = previousServerState match {
      case Some(oldServer) =>
        game.model.servers.map {
          case s if s.name == targetServerName => oldServer
          case s => s
        }
      case None => game.model.servers
    }

    game.copy(model = game.model.copy(players = players, servers = servers))
  }

  private def calculateRewards(server: Server, claimed: Boolean): ActionRewards = {
    import de.htwg.codebreaker.model.server.ServerType._
    
    // Wenn geclaimed: volle Belohnungen
    // Wenn nur Daten geklaut: halbe Belohnungen, aber mehr XP
    val multiplier = if (claimed) 1.0 else 0.5
    val xpBonus = if (claimed) 0 else 10
    
    server.serverType match {
      case Side     => ActionRewards(
        (server.rewardCpu * multiplier).toInt, 
        (server.rewardRam * multiplier).toInt, 
        0, 
        10 + xpBonus
      )
      case Firm     => ActionRewards(
        (server.rewardCpu * multiplier).toInt, 
        (server.rewardRam * multiplier).toInt, 
        0, 
        20 + xpBonus
      )
      case Cloud    => ActionRewards(
        (server.rewardCpu * multiplier).toInt, 
        (server.rewardRam * multiplier).toInt, 
        0, 
        30 + xpBonus
      )
      case Bank     => ActionRewards(
        0, 
        0, 
        (server.rewardCpu * multiplier).toInt, 
        40 + xpBonus
      )
      case Military => ActionRewards(
        (server.rewardCpu * 2 * multiplier).toInt, 
        (server.rewardRam * 2 * multiplier).toInt, 
        0, 
        50 + xpBonus
      )
      case GKS      => ActionRewards(0, 0, 0, 100 + xpBonus)
      case Private  => ActionRewards(0, 0, 0, 0)
    }
  }
}

case class ActionRewards(
  cpuGained: Int,
  ramGained: Int,
  codeGained: Int,
  xpGained: Int
)
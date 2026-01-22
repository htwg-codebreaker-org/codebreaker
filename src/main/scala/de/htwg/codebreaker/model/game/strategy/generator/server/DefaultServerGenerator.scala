package de.htwg.codebreaker.model.game.strategy.server

import de.htwg.codebreaker.model.map.{WorldMap, Tile, Continent}
import de.htwg.codebreaker.model.server.{ServerType, Server, ServerBlueprint}
import de.htwg.codebreaker.model.game.strategy.ServerGenerationStrategy  // ← HINZUFÜGEN!
import scala.util.Random

/** 
 * ServerGenerator implementiert die Standard-Server-Generierung.
 * Erzeugt Fixed Servers (aus Blueprints) und Side Servers (zufällig).
 */
object DefaultServerGenerator extends ServerGenerationStrategy:

  private val rng = new Random()

  // ═══════════════════════════════════════════════════════
  // STRATEGY INTERFACE IMPLEMENTATION
  // ═══════════════════════════════════════════════════════
  
  override def generateServers(map: WorldMap): List[Server] = {
    val continents = Continent.values.filter(_.isLand).toList
    val fixedServers = generateFixedServers(map)
    val sideServers = continents.flatMap(c => 
      generateSideServersFor(c, map, fixedServers)
    )
    fixedServers ++ sideServers
  }

  // ═══════════════════════════════════════════════════════
  // INTERNAL HELPER METHODS
  // ═══════════════════════════════════════════════════════

  private[model] def distance(a: Tile, b: Tile): Int =
    math.abs(a.x - b.x) + math.abs(a.y - b.y)

  private[model] def pickNonCloseTiles(
    tiles: Vector[Tile],
    count: Int,
    minDistance: Int,
    avoidTiles: List[Tile] = Nil
  ): Vector[Tile] = {
    val shuffled = rng.shuffle(tiles)
    shuffled.foldLeft(Vector.empty[Tile]) { (picked, next) =>
      if picked.size >= count then
        picked
      else if avoidTiles.contains(next) then
        picked
      else if (picked ++ avoidTiles).forall(t => distance(t, next) >= minDistance) then
        picked :+ next
      else
        picked
    }
  }

  private def generateSideServersFor(  // ← Private, nur intern verwendet
    continent: Continent,
    map: WorldMap,
    existingServers: List[Server],
    minDistance: Int = 2
  ): List[Server] = {
    val tiles      = map.tilesOf(continent)
    val fixedTiles = existingServers.map(_.tile)
    val count      = 3 + rng.nextInt(4)

    val pickedStrict = pickNonCloseTiles(tiles, count, minDistance, fixedTiles)

    val picked = 
      if pickedStrict.size >= count then pickedStrict 
      else {
        val freeTiles = tiles.filterNot(fixedTiles.contains)
        rng.shuffle(freeTiles).take(count).toVector
      }

    picked.zipWithIndex.map { case (tile, idx) =>
      val difficulty = 20 + rng.nextInt(31)
      val rewardCpu  = 10 + rng.nextInt(11)
      val rewardRam  = 10 + rng.nextInt(11)
      val name       = s"Nebenserver-${continent.short}-$idx"

      Server(
        name        = name,
        tile        = tile,
        difficulty  = difficulty,
        rewardCpu   = rewardCpu,
        rewardRam   = rewardRam,
        hacked      = false,
        serverType  = ServerType.Side,
        installedRole = None,
        claimedBy = None,
        hackedBy = None,
        cybersecurityLevel = 0,
        blockedUntilRound = None
      )
    }.toList
  }

  private def generateFixedServers(map: WorldMap): List[Server] =  // ← Private
    fixedBlueprints.map { bp =>
      val tile = map.tileAt(bp.preferredPosition._1, bp.preferredPosition._2).get

      Server(
        name        = bp.name,
        tile        = tile,
        difficulty  = rngIn(bp.difficultyRange),
        rewardCpu   = rngIn(bp.rewardCpuRange),
        rewardRam   = rngIn(bp.rewardRamRange),
        hacked      = false,
        serverType  = bp.serverType,
        installedRole = None,
        claimedBy = None,
        hackedBy = None,
        cybersecurityLevel = 0,
        blockedUntilRound = None
      )
    }

  private[model] def rngIn(range: (Int, Int)): Int =
    val (min, max) = range
    if min == max then min
    else rng.nextInt(max - min + 1) + min

  // ═══════════════════════════════════════════════════════
  // BLUEPRINTS
  // ═══════════════════════════════════════════════════════

  private val fixedBlueprints: List[ServerBlueprint] = List(
    ServerBlueprint("Pentagon",         (24, 14), ServerType.Military, (85, 90),  (30, 35), (50, 60)),
    ServerBlueprint("Wall Street",      (23, 14), ServerType.Bank,     (60, 75),  (20,  25), (25,  30)),
    ServerBlueprint("Silicon Valley",   (13, 15), ServerType.Cloud,    (60, 70),  (15,  20), (30,  35)),
    ServerBlueprint("Brussels",         (39, 11), ServerType.Bank,     (70, 80),  (30,  40), (15,  25)),
    ServerBlueprint("Frankfurt Hub",    (40, 12), ServerType.Bank,     (60, 75),  (25,  30), (15,  20)),
    ServerBlueprint("Moscow",           (46, 11), ServerType.Military, (60, 70),  (25,  30), (15,  20)),
    ServerBlueprint("Beijing",          (61, 15), ServerType.Military, (65, 75),  (30,  35), (20,  25)),
    ServerBlueprint("Tokyo Grid",       (66, 15), ServerType.Cloud,    (60, 70),  (20,  25), (15,  25)),
    ServerBlueprint("Sydney Core",      (67, 27), ServerType.Cloud,    (50, 65),  (20,  25), (20,  30)),
    ServerBlueprint("Cairo",            (44, 16), ServerType.Firm,     (40, 60),  (20,  30), (10,  20)),
    ServerBlueprint("GKS",              (11, 8),  ServerType.GKS,      (90,100),  (0,   0), (0,   0))
  )
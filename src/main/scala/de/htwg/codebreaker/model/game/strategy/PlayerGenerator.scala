package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model._
import scala.util.Random

object PlayerGenerator:

  private val initialSkills: Set[String] =
    Set("script_kiddie")

  def generatePlayers(count: Int, map: WorldMap, avoidTiles: List[Tile] = Nil): List[Player] =
    val rng = new Random()
    val validTiles = map.tiles.filter(t => t.continent.isLand && !avoidTiles.contains(t))
    val shuffled = rng.shuffle(validTiles).take(count)

    shuffled.zipWithIndex.map { case (tile, idx) =>
      Player(
        id = idx + 1,
        name = s"Spieler ${idx + 1}",
        tile = tile,
        cpu = 100,
        ram = 50,
        code = 10,
        availableXp = 0,
        totalXpEarned = 0,
        skills = PlayerSkillTree(initialSkills),
        cybersecurity = 20
      )
    }.toList

package de.htwg.codebreaker.model.game.strategy.player

import de.htwg.codebreaker.model.map.{WorldMap, Tile}
import de.htwg.codebreaker.model.player.{Player}
import de.htwg.codebreaker.model.player.skill.PlayerSkillTree
import de.htwg.codebreaker.model.player.laptop.{Laptop, LaptopHardware, LaptopInstalledTools}
import de.htwg.codebreaker.model.game.strategy.laptop.DefaultLaptopToolGenerator
import scala.util.Random

object PlayerGeneratorUnlockAll:

  private val initialSkills: Set[String] =
    Set("script_kiddie")

  private def initialLaptop = {
    // Generiere alle verfügbaren Tools
    val allTools = DefaultLaptopToolGenerator.generateLaptopTools()
    
    // Wähle einige Starter-Tools aus (z.B. Nmap und Wireshark)
    val starterTools = allTools.filter(tool => 
      tool.id == "nmap" || tool.id == "wireshark"
    )
    
    Laptop(
      hardware = LaptopHardware(
        cpu = 1000,
        ram = 1000,
        code = 1000,
        kerne = 1,
        networkRange = 1000
      ),
      tools = LaptopInstalledTools(starterTools),
      runningActions = Nil,
      cybersecurity = 1000
    )
  }

  def generatePlayers(count: Int, map: WorldMap, avoidTiles: List[Tile] = Nil): List[Player] =
    val rng = new Random()
    val validTiles = map.tiles.filter(t => t.continent.isLand && !avoidTiles.contains(t))
    val shuffled = rng.shuffle(validTiles).take(count)

    shuffled.zipWithIndex.map { case (tile, idx) =>
      Player(
        id = idx + 1,
        name = s"Spieler ${idx + 1}",
        tile = tile,
        laptop = initialLaptop,
        availableXp = 1000,
        totalXpEarned = 10001000,
        skills = PlayerSkillTree(initialSkills),
        movementPoints = 1000,
        maxMovementPoints = 1000,
        arrested = false
      )
    }.toList

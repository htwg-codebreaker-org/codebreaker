package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.map.{Tile, Continent, WorldMap}
import de.htwg.codebreaker.model.player.{Player}
import de.htwg.codebreaker.model.player.skill.PlayerSkillTree
import de.htwg.codebreaker.model.player.laptop.{Laptop, LaptopHardware, LaptopInstalledTools}
import de.htwg.codebreaker.model.server.{Server, ServerType}

object TestGameFactory:

  def game(): Game =
    val tile = Tile(0, 0, Continent.NorthAmerica)
    val hardware = LaptopHardware(cpu = 100, ram = 100, code = 0, kerne = 1, networkRange = 1)
    val tools = LaptopInstalledTools.empty
    val laptop = Laptop(hardware = hardware, tools = tools, runningActions = Nil, runningInternetSearch = None, cybersecurity = 10)
    val player = Player(
      id = 0,
      name = "TestPlayer",
      tile = tile,
      laptop = laptop,
      availableXp = 0,
      totalXpEarned = 0,
      skills = PlayerSkillTree(Set.empty),
      arrested = false,
      movementPoints = 5,
      maxMovementPoints = 5
    )

    val server = Server(
      name = "TestServer",
      tile = tile,
      difficulty = 50,
      rewardCpu = 10,
      rewardRam = 10,
      hacked = false,
      serverType = ServerType.Bank,
      hackedBy = None,
      claimedBy = None,
      cybersecurityLevel = 10,
      blockedUntilRound = None,
      installedRole = None
    )

    val worldMap = WorldMap.defaultMap
    val model = GameModel(List(player), List(server), worldMap, Nil, Nil, Nil, Nil, Nil)
    val state = GameState()

    Game(model, state)

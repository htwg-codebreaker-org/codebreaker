package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model._

object TestGameFactory:
  
  def game(): Game = 
    val tile = Tile(0, 0, Continent.NorthAmerica)
    val player = Player(
      id = 0,
      name = "TestPlayer",
      tile = tile,
      cpu = 100,
      ram = 100,
      code = 0,
      availableXp = 0,
      totalXpEarned = 0,
      skills = PlayerSkillTree(Set.empty),
      cybersecurity = 10,
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
      claimedBy = None
    )
    
    val worldMap = WorldMap.defaultMap
    val model = GameModel(List(player), List(server), worldMap, Nil)
    val state = GameState()
    
    Game(model, state)

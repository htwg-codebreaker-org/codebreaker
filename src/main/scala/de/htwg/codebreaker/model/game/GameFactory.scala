package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.generator._

object GameFactory:

  def createDefaultGame(): (GameModel, GameState) =
    val map = WorldMap.defaultMap

    // Spieler
    val player1 = Player(1, "Nico", map.tileAt(3, 2).get, 50, 20, 10, 1, 0, 20)
    val player2 = Player(2, "Henrik", map.tileAt(15, 6).get, 50, 20, 10, 1, 0, 20)
    val players = List(player1, player2)

    // Server
    val continents = Continent.values.filter(_.isLand).toList
    val fixedServers = ServerGenerator.generateFixedServers(map)
    val sideServers = continents.flatMap(c => ServerGenerator.generateSideServersFor(c, map, fixedServers))
    val allServers = fixedServers ++ sideServers

    val model = GameModel(players = players, servers = allServers, worldMap = map)
    val state = GameState() // Standardzustand

    (model, state)

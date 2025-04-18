package de.htwg.codebreaker

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.ui._

@main def runGame(): Unit =
  println("Willkommen zu Codebreaker!")
  println("Das Spiel wird gestartet...")
  println("Das Spiel wird geladen...")
  println("Map wird erstellt...")

  val map = WorldMap.defaultMap
  WorldMap.printContinentMap(map)

  println("Map wurde erstellt!")
  println("Spieler werden erstellt...")
  val player1 = Player(1, "Nico", map.tileAt(3, 2).get, 50, 20, 10, 1, 0, 20)
  val player2 = Player(2, "Henrik", map.tileAt(15, 6).get, 50, 20, 10, 1, 0, 20)

  val players = List(player1, player2)
  println("Spieler wurden erstellt!")


  
  

  // Generiere zufällige Server für jedes Land auf der Karte
  val continents = Continent.values.filter(_.isLand).toList

  val fixedServers = ServerGenerator.generateFixedServers(map)
  val randomSideServers = continents.flatMap(c =>
    ServerGenerator.generateSideServersFor(c, map, fixedServers)
  )

  /*val fixedServers = List(
    Server("Pentagon", map.tileAt(2, 2).get, 85, 30, 50, false, ServerType.Military),
    Server("GKS", map.tileAt(10, 5).get, 90, 0, 0, false, ServerType.GKS),
    Server("Silicon Valley", map.tileAt(1, 3).get, 60, 15, 30, false, ServerType.Cloud),
    Server("Mexico City", map.tileAt(2, 4).get, 20, 10, 10, false, ServerType.Private),
    Server("Brussels", map.tileAt(7, 2).get, 75, 40, 20, false, ServerType.Bank),
    Server("Cairo", map.tileAt(8, 5).get, 40, 20, 10, false, ServerType.Firm),
    Server("Istanbul", map.tileAt(9, 3).get, 50, 15, 20, false, ServerType.Firm),
    Server("Moscow", map.tileAt(10, 2).get, 60, 25, 15, false, ServerType.Military),
    Server("Beijing", map.tileAt(13, 3).get, 70, 30, 20, false, ServerType.Military)
  )*/


  val allServers = fixedServers ++ randomSideServers

  TUI.show(players, map, allServers)
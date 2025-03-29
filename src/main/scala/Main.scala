import model._
import ui.TUI

@main def runGame(): Unit =
  println("Willkommen zu Codebreaker!")
  val player1 = Player("Nico", (3, 2), 50, 20, 10, 1, 0, 20)
  val player2 = Player("Henrik", (15, 6), 50, 20, 10, 1, 0, 20)
  val players = List(player1, player2)
  

  val map = WorldMap.defaultMap
  WorldMap.printContinentMap(map)

  val continents = Continent.values.filter(_.isLand).toList

  // Generiere zufällige Server für jedes Land auf der Karte
  val randomSideServers = continents.flatMap(c => ServerGenerator.generateSideServersFor(c, map))

  val fixedServers = List(
    Server("Pentagon", (2, 2), 85, 30, 50, false, ServerType.Military),
    Server("GKS", (10, 5), 90, 0, 0, false, ServerType.GKS),
    Server("Silicon Valley", (1, 3), 60, 15, 30, false, ServerType.Cloud),
    Server("Mexico City", (2, 4), 20, 10, 10, false, ServerType.Private),
    Server("Brussels", (7, 2), 75, 40, 20, false, ServerType.Bank),
    Server("Cairo", (8, 5), 40, 20, 10, false, ServerType.Firm),
    Server("Istanbul", (9, 3), 50, 15, 20, false, ServerType.Firm),
    Server("Moscow", (10, 2), 60, 25, 15, false, ServerType.Military),
    Server("Beijing", (13, 3), 70, 30, 20, false, ServerType.Military)
  )

  val allServers = fixedServers ++ randomSideServers

  TUI.show(players, map, allServers)
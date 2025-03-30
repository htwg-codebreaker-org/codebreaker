package model

import scala.util.Random
import model._

object ServerGenerator:

  def generateSideServersFor(continent: Continent, map: WorldMap): List[Server] =
    val rng = new Random()
    val tiles = map.tilesOf(continent)
    val count = 3 + rng.nextInt(4) // 3 bis 6

    val pickedTiles = rng.shuffle(tiles).take(count)

    pickedTiles.zipWithIndex.map { case (tile, idx) =>
      val difficulty = 20 + rng.nextInt(31) // 20–50
      val rewardCpu = 10 + rng.nextInt(11)  // 10–20
      val rewardRam = 10 + rng.nextInt(11)
      val rewardCode = 10 + rng.nextInt(11)
      val rewardMoney = rng.nextInt(11)  // 0-10
      val rewardXp = rng.nextInt(11)  // 0-10


      Server(
        name = s"Nebenserver-${continent.toString.take(2)}-$idx",
        position = (tile.x, tile.y),
        difficulty = difficulty,
        rewardCpu = rewardCpu,
        rewardRam = rewardRam,
        rewardCode = rewardCode,
        rewardMoney = rewardMoney,
        rewardXp = rewardXp,
        hacked = false,
        serverType = ServerType.Side
      )
    }.toList

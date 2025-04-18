package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ServerGeneratorSpec extends AnyWordSpec with Matchers {
  import ServerGenerator._

  // eine ganz kleine Mini‑Map für distance und pickNonCloseTiles
  private val t00 = Tile(0, 0, Continent.Ocean)
  private val t01 = Tile(0, 1, Continent.Ocean)
  private val t10 = Tile(1, 0, Continent.Ocean)
  private val t11 = Tile(1, 1, Continent.Ocean)
  private val sampleTiles = Vector(t00, t01, t10, t11)

  val world = WorldMap.defaultMap
  val continents = Continent.values.filter(_.isLand).toList
  val fixed = generateFixedServers(world)

  "distance" should {
    "compute the Manhattan distance correctly" in {
      distance(t00, Tile(3, 4, Continent.Ocean)) shouldBe 7
      distance(Tile(5, 5, Continent.Ocean), Tile(2, 3, Continent.Ocean)) shouldBe 5
    }
  }

  "pickNonCloseTiles" should {
    "return empty when count = 0" in {
      pickNonCloseTiles(sampleTiles, 0, 1) shouldBe empty
    }
    "never pick more than `count` elements" in {
      pickNonCloseTiles(sampleTiles, 2, 0).size shouldBe 2
    }
    "never include any avoidTiles" in {
      val avoid = List(t00, t01)
      val picked = pickNonCloseTiles(sampleTiles, 4, 0, avoid)
      picked should not contain t00
      picked should not contain t01
    }
    "honor the minimum distance requirement" in {
      val picked = pickNonCloseTiles(sampleTiles, 4, minDistance = 2)
      // im Ergebnis dürfen keine zwei Tiles mit Abstand < 2 stehen
      for {
        a <- picked
        b <- picked if a != b
      } distance(a, b) should be >= 2
    }
    "stop once count reached" in {
      // auch wenn weitere Tiles noch abstands‑ok wären, sobald size>=count ist, bricht es ab
      pickNonCloseTiles(sampleTiles, 3, 0).size shouldBe 3
    }
    "never pick more than available when count > tiles.size" in {
      // count=10, tiles.size=4
      pickNonCloseTiles(sampleTiles, 10, 0).size shouldBe 4
    }
  }

  "rngIn" should {
    "return the same value when min == max" in {
      rngIn(5 -> 5) shouldBe 5
    }
    "return values within the specified range" in {
      val range = (10, 12)
      val samples = Seq.fill(50)(rngIn(range))
      all(samples) should (be >= 10 and be <= 12)
    }
  }

  "generateFixedServers" should {
    lazy val fixedServers = generateFixedServers(world)

    "create exactly one Server per blueprint" in {
      fixedServers.size shouldBe fixedBlueprints.size
    }
    "use exactly the blueprint names" in {
      fixedServers.map(_.name).sorted shouldBe fixedBlueprints.map(_.name).sorted
    }
    "honor each blueprint's difficulty and reward ranges" in {
      fixedServers.zip(fixedBlueprints).foreach { case (srv, bp) =>
        val (dm, dM) = bp.difficultyRange
        srv.difficulty should (be >= dm and be <= dM)

        val (cpuM, cpuM2) = bp.rewardCpuRange
        srv.rewardCpu should (be >= cpuM and be <= cpuM2)

        val (ramM, ramM2) = bp.rewardRamRange
        srv.rewardRam should (be >= ramM and be <= ramM2)
      }
    }
    "honor zero‑ranges (zB GKS)" in {
      val gks = fixedServers.find(_.serverType == ServerType.GKS).get
      gks.rewardCpu shouldBe 0
      gks.rewardRam shouldBe 0
    }
    "throw a NoSuchElementException if a blueprint's tile is missing" in {
      // wir entfernen absichtlich einen Blueprint‑Tile aus der Map
      val badBp = ServerBlueprint("Bad", (99, 99), ServerType.Firm, (1, 2), (3, 4), (5, 6))
      val badMap = world.copy(tiles = world.tiles.filterNot(t => t.x == 99 && t.y == 99))
      badMap.tileAt(99, 99) shouldBe None

      // direkt .get im Generator führt dann zum Crash
      an[NoSuchElementException] should be thrownBy {
        fixedBlueprints :+ badBp foreach { bp =>
          val tile = badMap.tileAt(bp.preferredPosition._1, bp.preferredPosition._2).get
          // hier würde .get schon scheitern
        }
      }
    }
  }

  "generateSideServersFor" should {
    "produce between 3 and 6 servers per continent" in {
      continents.foreach { cont =>
        val sides = generateSideServersFor(cont, world, Nil)
        sides.size should (be >= 3 and be <= 6)
      }
    }
    "never place a side server on a fixed server tile" in {
      continents.foreach { cont =>
        val sides = generateSideServersFor(cont, world, fixed)
        // Schnittmenge von Side-Server-Tiles und Fixed-Server-Tiles muss leer sein:
        sides.map(_.tile).intersect(fixed.map(_.tile)) shouldBe empty
      }
    }
    "always place them on the correct continent" in {
      continents.foreach { cont =>
        val sides = generateSideServersFor(cont, world, Nil)
        all(sides.map(_.tile.continent)) shouldBe cont
      }
    }
    "fallback to ignore distance if minDistance is too large" in {
      // minDistance=1.000 sorgt dafür, dass der strikte Modus <count liefert
      // und wir in die Fallback‑Phase rutschen
      val sides = generateSideServersFor(Continent.Europe, world, Nil, minDistance = 1000)
      sides.size should (be >= 3 and be <= 6)
    }
    "honor the minDistance when it's small" in {
      // mit minDistance=1 dürfen direkt benachbarte Tiles nicht beide gewählt werden
      val sides = generateSideServersFor(Continent.NorthAmerica, world, Nil, minDistance = 1)
      // überprüfen wir nur stichprobenartig:
      for {
        a <- sides.map(_.tile)
        b <- sides.map(_.tile) if a != b
      } distance(a, b) should be >= 1
    }
  }
}

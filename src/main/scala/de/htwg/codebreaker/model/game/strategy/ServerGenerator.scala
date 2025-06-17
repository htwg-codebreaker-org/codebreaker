package de.htwg.codebreaker.model.game.strategy
import de.htwg.codebreaker.model._
import scala.util.Random

/** 
 * ServerGenerator kümmert sich um die Erzeugung von Servern auf der Karte:
 * - Fixed Servers: Aus einer vordefinierten Liste von Blueprints entstehen immer die gleichen Kern-Server (z.B. "Pentagon").
 * - Side Servers: Zufällig verteilte Neben-Server pro Kontinent, mit Mindestabstand zueinander und zu den Fixed Servers.
 */
object ServerGenerator:

  // Zufallszahlengenerator für alle zufälligen Aspekte
  private val rng = new Random()

  /**
   * Manhattan‐Abstand zwischen zwei Tiles:
   * distance((x1,y1), (x2,y2)) = |x1-x2| + |y1-y2|
   * Wird genutzt, um Mindestabstände zwischen Servern sicherzustellen.
   */
  private[model] def distance(a: Tile, b: Tile): Int =
    math.abs(a.x - b.x) + math.abs(a.y - b.y)

  /**
   * Wählt bis zu `count` verschiedene Tiles aus `tiles` aus,
   * so dass jeder neu hinzugefügte Tile mindestens `minDistance`
   * (Manhattan‐Abstand) zu allen bereits gewählten und zusätzlich
   * zu den in `avoidTiles` übergebenen Tiles steht.
   *
   * Ablauf:
   * 1. `tiles` wird zufällig durchmischt.
   * 2. Jeder Tile aus dem Shuffle wird nacheinander geprüft:
   *    - Haben wir bereits `count` Tiles? Dann Abbruch.
   *    - Steht `next` mindestens `minDistance` von allen bisher
   *      gewählten (`picked`) und `avoidTiles` entfernt? Dann hinzufügen.
   *    - Andernfalls überspringen.
   */
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
        // überspringe alle Tiles, die in der Avoid‑Liste sind
        picked
      else if (picked ++ avoidTiles).forall(t => distance(t, next) >= minDistance) then
        picked :+ next
      else
        picked
    }
  }


  /**
   * Generiert pro Kontinent eine zufällige Anzahl (3–6) von Neben‐Servern.
   *
   * @param continent      Kontinent, auf dem die Neben‐Server entstehen.
   * @param map            die Weltkarte, um alle Tiles dieses Kontinents abzufragen.
   * @param existingServers bereits platzierte Server (werden als `avoidTiles` verwendet,
   *                        damit keine Side‐Server drauf stehen).
   * @param minDistance    minimale Manhattan‐Distanz zwischen allen Side‐Servern
   *                       und zu den `existingServers` (Standard: 2).
   * @return               Liste neu erzeugter Server mit dynamischem Namen,
   *                       Zufalls‐Difficulty und -Rewards.
   */
  def generateSideServersFor(
    continent: Continent,
    map: WorldMap,
    existingServers: List[Server],
    minDistance: Int = 2
  ): List[Server] =

    // 1) Basisdaten
    val tiles      = map.tilesOf(continent)
    val fixedTiles = existingServers.map(_.tile)
    // count ist zufällig zwischen 3 und 6
    val count      = 3 + rng.nextInt(4)

    // 2) Versuche erst mit Mindestabstand
    val pickedStrict = pickNonCloseTiles(tiles, count, minDistance, fixedTiles)

    // 3) Fallback falls zu wenige: nur FixedTiles meiden, aber Abstand ignorieren
    val picked = 
      if pickedStrict.size >= count then pickedStrict 
      else {
        val freeTiles = tiles.filterNot(fixedTiles.contains)
        // Falls selbst hier freeTiles.size < count, dann werden so viele wie möglich genommen
        rng.shuffle(freeTiles).take(count).toVector
      }

    // 4) Aus den ausgewählten Tiles endgültige Server basteln
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
        serverType  = ServerType.Side
      )
    }.toList


  /**
   * Erzeugt alle Fixed‐Server gemäß den `fixedBlueprints`.
   * Jeder Blueprint gibt einen Namen, eine feste Position und Werte‐Spannen vor.
   * Die tatsächlichen Difficulty‐ und Reward‐Werte werden per `rngIn` gezogen.
   */
  def generateFixedServers(map: WorldMap): List[Server] =
    fixedBlueprints.map { bp =>
      // Blueprint‐Position muss existieren, sonst gibt .get eine Exception
      val tile = map.tileAt(bp.preferredPosition._1, bp.preferredPosition._2).get

      Server(
        name        = bp.name,
        tile        = tile,
        difficulty  = rngIn(bp.difficultyRange),
        rewardCpu   = rngIn(bp.rewardCpuRange),
        rewardRam   = rngIn(bp.rewardRamRange),
        hacked      = false,
        serverType  = bp.serverType
      )
    }

  /**
   * Ziehe eine Zufallszahl im gegebenen inklusiven Bereich (min, max).
   * Wenn min == max, gib direkt den einzigen Wert zurück.
   */
  private[model] def rngIn(range: (Int, Int)): Int =
    val (min, max) = range
    if min == max then min
    else rng.nextInt(max - min + 1) + min

  // --------------------------------------------
  // Vordefinierte Blueprints für Fixed‐Server
  // --------------------------------------------
  // Die Koordinaten basieren auf der Continent‐Einteilung in WorldMap.classifyContinent.
  val fixedBlueprints: List[ServerBlueprint] = List(
    // USA: Pentagon (Washington DC), Wall Street (NYC), Silicon Valley (Kalifornien)
    ServerBlueprint("Pentagon",         (24, 14), ServerType.Military, (85, 90),  (30, 35), (50, 60)), // Ostküste, Nordamerika
    ServerBlueprint("Wall Street",      (23, 14), ServerType.Bank,     (60, 75),  (20,  25), (25,  30)), // NYC, Nordamerika
    ServerBlueprint("Silicon Valley",   (13, 15), ServerType.Cloud,    (60, 70),  (15,  20), (30,  35)), // Westküste, Nordamerika
    // Europa: Brüssel, Frankfurt
    ServerBlueprint("Brussels",         (39, 11), ServerType.Bank,     (70, 80),  (30,  40), (15,  25)), // Belgien, Europa
    ServerBlueprint("Frankfurt Hub",    (40, 12), ServerType.Bank,     (60, 75),  (25,  30), (15,  20)), // Deutschland, Europa
    // Russland: Moskau
    ServerBlueprint("Moscow",           (46, 11), ServerType.Military, (60, 70),  (25,  30), (15,  20)), // Russland, Asien
    // China: Beijing
    ServerBlueprint("Beijing",          (61, 15), ServerType.Military, (65, 75),  (30,  35), (20,  25)), // China, Asien
    // Japan: Tokyo
    ServerBlueprint("Tokyo Grid",       (66, 15), ServerType.Cloud,    (60, 70),  (20,  25), (15,  25)), // Japan, Asien
    // Australien: Sydney
    ServerBlueprint("Sydney Core",      (67, 27), ServerType.Cloud,    (50, 65),  (20,  25), (20,  30)), // Australien, Ozeanien
    // Afrika: Kairo
    ServerBlueprint("Cairo",            (44, 16), ServerType.Firm,     (40, 60),  (20,  30), (10,  20)), // Ägypten, Afrika
    // Deutschland: GKS (fiktiv, zentral in Europa)
    ServerBlueprint("GKS",              (11, 8), ServerType.GKS,      (90,100),  (0,   0), (0,   0))    // Deutschland, Europa
  )

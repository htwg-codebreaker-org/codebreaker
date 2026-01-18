// src/main/scala/de/htwg/codebreaker/model/MapObject.scala
package de.htwg.codebreaker.model.map
import de.htwg.codebreaker.model.server.ServerType

/**
 * Verschiedene Objekte, die auf einer Weltkarten‑Zelle liegen können:
 * - Spieler
 * - Server
 * - Beides
 * - Nichts
 */
sealed trait MapObject

object MapObject:

  /** Ein Spieler steht allein auf der Kachel. */
  case class PlayerOnTile(index: Int) extends MapObject

  /** Ein Server steht allein auf der Kachel. */
  case class ServerOnTile(
    index: Int,
    serverType: ServerType,
    continent: Continent
  ) extends MapObject

  /** Spieler und Server teilen sich dieselbe Kachel. */
  case class PlayerAndServerTile(
    playerIndex: Int,
    serverIndex: Int,
    serverType: ServerType,
    continent: Continent
  ) extends MapObject

  /** Keine Objekte auf der Kachel auf Land. */
  case class EmptyTile(continent: Continent) extends MapObject

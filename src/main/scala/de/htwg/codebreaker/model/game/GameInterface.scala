package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model._

/** Interface for the Game component. Provides read-only access to game data without exposing
  * internal structure. This interface ensures that external components cannot directly modify game
  * state.
  */
trait GameInterface:
  def getPlayers: List[Player]
  def getServers: List[Server]
  def getWorldMap: WorldMapInterface
  def getState: GameState

/** Interface for the WorldMap component. Encapsulates world map data and operations.
  */
trait WorldMapInterface:
  def getMapData(players: List[Player], servers: List[Server]): Vector[Vector[MapObject]]
  def getTile(x: Int, y: Int): Option[Tile]
  def getWidth: Int
  def getHeight: Int

package de.htwg.codebreaker.model.api

import de.htwg.codebreaker.model.{Continent, MapObject}

trait IWorldMap:
  def width: Int
  def height: Int
  def tiles: Vector[ITile]

  def getMapData(players: List[IPlayer], servers: List[IServer]): Vector[Vector[MapObject]]
  def tileAt(x: Int, y: Int): Option[ITile]
  def tilesOf(continent: Continent): Vector[ITile]
  def continentAt(x: Int, y: Int): Option[Continent]

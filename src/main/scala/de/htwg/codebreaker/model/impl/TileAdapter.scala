// TileAdapter.scala
package de.htwg.codebreaker.model.impl

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.api.ITile

case class TileAdapter(tile: Tile) extends ITile:
  def x: Int = tile.x
  def y: Int = tile.y
  def continent: Continent = tile.continent
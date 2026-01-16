// src/main/scala/de/htwg/codebreaker/model/Tile.scala
package de.htwg.codebreaker.model

/**
 * Eine Kachel auf der Weltkarte.
 *
 * @param x, y      Koordinaten (0..width-1, 0..height-1)
 * @param continent Kontinent, zu dem diese Kachel gehört
 */
case class Tile(x: Int, y: Int, continent: Continent)

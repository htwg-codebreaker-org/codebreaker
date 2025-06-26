package de.htwg.codebreaker.model.api

import de.htwg.codebreaker.model.Continent

trait ITile:
  def x: Int
  def y: Int
  def continent: Continent

// src/main/scala/de/htwg/codebreaker/model/game/strategy/LaptopToolGenerationStrategy.scala
package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model.player.laptop.LaptopTool

trait LaptopToolGenerationStrategy:
  def generateLaptopTools(): List[LaptopTool]
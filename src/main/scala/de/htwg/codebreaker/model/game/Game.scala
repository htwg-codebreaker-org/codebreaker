// src/main/scala/de/htwg/codebreaker/model/game/Game.scala
package de.htwg.codebreaker.model.game
import de.htwg.codebreaker.model.game.GameModel
import de.htwg.codebreaker.model.game.GameState

case class Game(
  model: GameModel,
  state: GameState
)


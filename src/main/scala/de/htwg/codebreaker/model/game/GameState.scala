package de.htwg.codebreaker.model.game
import de.htwg.codebreaker.model._

enum GameStatus:
  case Running, Paused, GameOver

enum Phase:
  case AwaitingInput, ExecutingTurn, FinishedTurn

case class GameState(
  var currentPlayerIndex: Option[Int] = None,
  var status: GameStatus = GameStatus.Running,
  var phase: Phase = Phase.AwaitingInput,
  var round: Int = 1
)



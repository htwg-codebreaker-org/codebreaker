package de.htwg.codebreaker.model.game
import de.htwg.codebreaker.model._

enum GameStatus:
  case Running, Paused, GameOver

enum Phase:
  case AwaitingInput, ExecutingTurn, FinishedTurn

case class GameState(
  currentPlayerIndex: Option[Int] = None,
  status: GameStatus = GameStatus.Running,
  phase: Phase = Phase.AwaitingInput,
  round: Int = 1
)

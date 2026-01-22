// === src/main/scala/de/htwg/codebreaker/model/game/GameState.scala ===
package de.htwg.codebreaker.model.game

enum GameStatus:
  case Running, Paused, GameOver

enum Phase:
  case AwaitingInput, ExecutingTurn, FinishedTurn

case class GameState(
  currentPlayerIndex: Option[Int] = None,
  status: GameStatus = GameStatus.Running,
  phase: Phase = Phase.AwaitingInput,
  round: Int = 1
):

  def advanceRound(): GameState =
    copy(round = round + 1)

  def setStatus(newStatus: GameStatus): GameState =
    copy(status = newStatus)

  def setPhase(newPhase: Phase): GameState =
    copy(phase = newPhase)
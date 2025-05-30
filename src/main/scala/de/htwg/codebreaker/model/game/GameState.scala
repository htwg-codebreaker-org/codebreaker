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
  def nextPlayer(totalPlayers: Int): GameState =
    val nextIndex = currentPlayerIndex match
      case Some(i) => (i + 1) % totalPlayers
      case None    => 0
    copy(currentPlayerIndex = Some(nextIndex))

  def advanceRound(): GameState =
    copy(round = round + 1)

  def setStatus(newStatus: GameStatus): GameState =
    copy(status = newStatus)

  def setPhase(newPhase: Phase): GameState =
    copy(phase = newPhase)

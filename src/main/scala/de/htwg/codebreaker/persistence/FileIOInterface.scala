package de.htwg.codebreaker.persistence

import de.htwg.codebreaker.model.game.Game

import scala.util.Try

/** Interface for file I/O operations. Defines methods for saving and loading game state.
  */
trait FileIOInterface:
  /** Saves the game state to a file.
    * @param game
    *   The game to save
    * @return
    *   Try[Unit] - Success if saved successfully, Failure otherwise
    */
  def save(game: Game): Try[Unit]

  /** Loads the game state from a file.
    * @return
    *   Try[Game] - Success with loaded game, Failure if file doesn't exist or is invalid
    */
  def load(): Try[Game]

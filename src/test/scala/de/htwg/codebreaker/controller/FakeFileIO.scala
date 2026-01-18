package de.htwg.codebreaker.controller

import de.htwg.codebreaker.persistence.FileIOInterface
import de.htwg.codebreaker.model.game.game.Game
import scala.util.{Try, Success, Failure}

class FakeFileIO extends FileIOInterface:
  
  private var savedGame: Option[Game] = None
  
  override def save(game: Game): Try[Unit] = 
    savedGame = Some(game)
    Success(())
  
  override def load(): Try[Game] = 
    savedGame match
      case Some(game) => Success(game)
      case None => Failure(new Exception("No saved game"))

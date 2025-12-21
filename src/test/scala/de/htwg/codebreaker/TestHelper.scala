package de.htwg.codebreaker

import de.htwg.codebreaker.persistence.FileIOInterface
import de.htwg.codebreaker.model.game.Game
import scala.util.{Try, Success, Failure}

object TestHelper {
  /** Mock FileIO for testing that does nothing */
  val mockFileIO: FileIOInterface = new FileIOInterface {
    private var savedGame: Option[Game] = None

    def save(game: Game): Try[Unit] = {
      savedGame = Some(game)
      Success(())
    }

    def load(): Try[Game] = savedGame match {
      case Some(g) => Success(g)
      case None => Failure(new Exception("No game saved yet"))
    }
  }
}

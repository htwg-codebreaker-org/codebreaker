package de.htwg.codebreaker.controller.controller

import com.typesafe.scalalogging.LazyLogging
import de.htwg.codebreaker.util.Observer
import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.model.map.{MapObject}
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.game.game.{Game, GameState, Phase, GameStatus}
import de.htwg.codebreaker.controller.Command

class LoggingController(inner: ControllerInterface)
  extends ControllerInterface
    with LazyLogging {

  logger.info("LoggingController enabled")

  // Observer
  override def add(o: Observer): Unit =
    inner.add(o)

  override def remove(o: Observer): Unit =
    inner.remove(o)

  // Queries
  override def game: Game =
    inner.game

  override def getPlayers: List[Player] =
    inner.getPlayers

  override def getServers: List[Server] =
    inner.getServers

  override def getMapData(): Vector[Vector[MapObject]] =
    inner.getMapData()

  override def getState: GameState =
    inner.getState

  override def canUndo: Boolean =
    inner.canUndo

  override def canRedo: Boolean =
    inner.canRedo

  // Commands
  override def doAndRemember(cmd: Command): Unit = {
    logger.info(s"doAndRemember: ${cmd.getClass.getSimpleName}")
    inner.doAndRemember(cmd)
  }
  
  override def doAndForget(cmd: Command): Unit = {
    logger.info(s"doAndForget: ${cmd.getClass.getSimpleName}")
    inner.doAndForget(cmd)
  }
  override def undo(): Unit = {
    logger.info("undo")
    inner.undo()
  }

  override def redo(): Unit = {
    logger.info("redo")
    inner.redo()
  }


  // State changes
  override def setPhase(newPhase: Phase): Unit = {
    logger.info(s"setPhase: $newPhase")
    inner.setPhase(newPhase)
  }

  override def setStatus(newStatus: GameStatus): Unit = {
    logger.info(s"setStatus: $newStatus")
    inner.setStatus(newStatus)
  }

  override def setGame(newGame: Game): Unit = {
    logger.info("setGame")
    inner.setGame(newGame)
  }

  // Persistence
  override def save(): Unit = {
    logger.info("save")
    inner.save()
  }

  override def load(): Unit = {
    logger.info("load")
    inner.load()
  }
}

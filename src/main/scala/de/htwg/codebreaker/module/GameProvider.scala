package de.htwg.codebreaker.module

import com.google.inject.Provider
import de.htwg.codebreaker.model.game.{Game, GameFactory}

/**
 * Provider for Game instances.
 * This allows Guice to create Game instances using the GameFactory.
 */
class GameProvider extends Provider[Game]:
  override def get(): Game =
    GameFactory("default")

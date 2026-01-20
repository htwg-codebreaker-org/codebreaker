package de.htwg.codebreaker.module

import com.google.inject.Provider
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.game.GameFactory
import de.htwg.codebreaker.model.game.GameFactory.apply
import de.htwg.codebreaker.model.game.GameFactory.unlockAll

/**
 * Provider for Game instances.
 * This allows Guice to create Game instances using the GameFactory.
 * "default"
 * "unlockAll"
 */
class GameProvider extends Provider[Game]:
  override def get(): Game =
    // You can modify this to choose different game types based on configuration or other criteria
    apply("default")

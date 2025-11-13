package de.htwg.codebreaker.module

import com.google.inject.AbstractModule
import de.htwg.codebreaker.controller.{Controller, ControllerInterface}
import de.htwg.codebreaker.model.game.Game
import com.google.inject.Scopes

/**
 * Guice Module for the Codebreaker application.
 * Defines all dependency injection bindings between interfaces and implementations.
 *
 * This module:
 * - Binds ControllerInterface to Controller implementation
 * - Provides Game instances via GameProvider
 * - Uses singleton scope for shared components (Controller)
 */
class CodebreakerModule extends AbstractModule:

  override def configure(): Unit =
    // Bind Game using custom provider
    bind(classOf[Game]).toProvider(classOf[GameProvider])

    // Bind ControllerInterface to Controller implementation as singleton
    // Singleton ensures TUI and GUI share the same controller instance
    bind(classOf[ControllerInterface]).to(classOf[Controller]).in(Scopes.SINGLETON)

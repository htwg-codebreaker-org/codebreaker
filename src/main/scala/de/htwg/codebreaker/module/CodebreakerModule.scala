package de.htwg.codebreaker.module

import com.google.inject.{AbstractModule, Provides}
import com.google.inject.Scopes
import de.htwg.codebreaker.controller._
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.persistence._
import de.htwg.codebreaker.controller.controller.Controller
import de.htwg.codebreaker.controller.controller.LoggingController


/**
 * Guice Module for the Codebreaker application.
 * Defines all dependency injection bindings between interfaces and implementations.
 *
 * This module:
 * - Provides ControllerInterface via DI (optionally wrapped with LoggingController)
 * - Uses a DI flag to enable/disable controller logging
 * - Provides Game instances via GameProvider
 * - Binds FileIOInterface to either JSON or XML implementation (choose one)
 * - Uses singleton scope for shared components (Controller)
 *
 * To switch between JSON and XML, change the binding below:
 * - For JSON: bind(classOf[FileIOInterface]).to(classOf[FileIOJSON])
 * - For XML:  bind(classOf[FileIOInterface]).to(classOf[FileIOXML])
 */
class CodebreakerModule extends AbstractModule:

  // 🔁 FLAG – exakt wie in der Vorlesung gemeint
  private val enableControllerLogging = true

  override def configure(): Unit =
    // Bind Game using custom provider
    bind(classOf[Game]).toProvider(classOf[GameProvider])

    // Bind FileIO implementation - CHANGE HERE TO SWITCH BETWEEN JSON/XML
    // Current: JSON (use FileIOXML for XML)
    bind(classOf[FileIOInterface]).to(classOf[FileIOJSON])

    // Bind the concrete Controller as singleton (shared by TUI and GUI)
    bind(classOf[Controller]).in(Scopes.SINGLETON)

  // provider method to conditionally wrap Controller with LoggingController
  @Provides
  @com.google.inject.Singleton
  def provideControllerInterface(
    controller: Controller
  ): ControllerInterface =
    if enableControllerLogging then
      new LoggingController(controller)
    else
      controller
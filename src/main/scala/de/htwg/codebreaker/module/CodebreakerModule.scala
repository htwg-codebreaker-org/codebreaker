package de.htwg.codebreaker.module

import com.google.inject.AbstractModule
import de.htwg.codebreaker.controller.{Controller, ControllerInterface}
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.persistence.{FileIOInterface, FileIOJSON, FileIOXML}
import com.google.inject.Scopes

/**
 * Guice Module for the Codebreaker application.
 * Defines all dependency injection bindings between interfaces and implementations.
 *
 * This module:
 * - Binds ControllerInterface to Controller implementation
 * - Provides Game instances via GameProvider
 * - Binds FileIOInterface to either JSON or XML implementation (choose one)
 * - Uses singleton scope for shared components (Controller)
 *
 * To switch between JSON and XML, change the binding below:
 * - For JSON: bind(classOf[FileIOInterface]).to(classOf[FileIOJSON])
 * - For XML:  bind(classOf[FileIOInterface]).to(classOf[FileIOXML])
 */
class CodebreakerModule extends AbstractModule:

  override def configure(): Unit =
    // Bind Game using custom provider
    bind(classOf[Game]).toProvider(classOf[GameProvider])

    // Bind FileIO implementation - CHANGE HERE TO SWITCH BETWEEN JSON/XML
    // Current: JSON (use FileIOXML for XML)
    bind(classOf[FileIOInterface]).to(classOf[FileIOJSON])

    // Bind ControllerInterface to Controller implementation as singleton
    // Singleton ensures TUI and GUI share the same controller instance
    bind(classOf[ControllerInterface]).to(classOf[Controller]).in(Scopes.SINGLETON)

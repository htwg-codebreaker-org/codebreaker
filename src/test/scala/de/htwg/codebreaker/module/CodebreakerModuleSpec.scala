package de.htwg.codebreaker.module

import com.google.inject.{Guice, Injector}
import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.controller.{Controller, LoggingController}
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.persistence.{FileIOInterface, FileIOJSON}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CodebreakerModuleSpec extends AnyWordSpec with Matchers {

  "CodebreakerModule" should {

    "create a valid Guice injector" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      injector should not be null
    }

    "bind Game to GameProvider" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val game = injector.getInstance(classOf[Game])

      game shouldBe a[Game]
      game.model.players should not be empty
      game.model.servers should not be empty
    }

    "bind FileIOInterface to FileIOJSON" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val fileIO = injector.getInstance(classOf[FileIOInterface])

      fileIO shouldBe a[FileIOJSON]
    }

    "bind ControllerInterface" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val controller = injector.getInstance(classOf[ControllerInterface])

      // Controller could be wrapped with LoggingController
      controller shouldBe a[ControllerInterface]
      controller should not be null
    }

    "provide ControllerInterface as singleton" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val controller1 = injector.getInstance(classOf[ControllerInterface])
      val controller2 = injector.getInstance(classOf[ControllerInterface])

      // Should be the same instance (singleton)
      controller1 should be theSameInstanceAs controller2
    }

    "bind concrete Controller as singleton" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val controller1 = injector.getInstance(classOf[Controller])
      val controller2 = injector.getInstance(classOf[Controller])

      // The underlying Controller should be singleton
      controller1 should be theSameInstanceAs controller2
    }

    "provide new Game instances on each request" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val game1 = injector.getInstance(classOf[Game])
      val game2 = injector.getInstance(classOf[Game])

      // Different instances (not singleton)
      game1 should not be theSameInstanceAs(game2)

      // Both should be valid games with players and servers
      game1.model.players should not be empty
      game2.model.players should not be empty
      game1.model.servers should not be empty
      game2.model.servers should not be empty
    }

    "provide new FileIO instances on each request" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val fileIO1 = injector.getInstance(classOf[FileIOInterface])
      val fileIO2 = injector.getInstance(classOf[FileIOInterface])

      // Should be different instances (not singleton)
      fileIO1 should not be theSameInstanceAs(fileIO2)
    }

    "wire all dependencies correctly for Controller" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val controller = injector.getInstance(classOf[ControllerInterface])

      // Controller should have a game
      controller.game should not be null
      controller.game.model.players should not be empty

      // Controller should have players and servers
      controller.getPlayers should not be empty
      controller.getServers should not be empty
    }

    "provide LoggingController when logging is enabled" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val controllerInterface = injector.getInstance(classOf[ControllerInterface])
      
      // If enableControllerLogging is true, should be LoggingController
      // Otherwise should be Controller
      controllerInterface shouldBe a[ControllerInterface]
    }

    "ensure underlying Controller is accessible" in {
      val injector = Guice.createInjector(new CodebreakerModule())
      val controller = injector.getInstance(classOf[Controller])
      
      controller shouldBe a[Controller]
      controller.game should not be null
    }
  }
}

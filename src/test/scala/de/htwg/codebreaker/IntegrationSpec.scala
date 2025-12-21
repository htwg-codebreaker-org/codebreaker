package de.htwg.codebreaker

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.controller._

class IntegrationSpec extends AnyWordSpec with Matchers {

  "A complete game flow" should {
    "allow creating a game, claiming servers, and using undo/redo" in {
      // Create a game using the factory
      val game = GameFactory("default")
      game should not be null

      // Create controller
      val controller = Controller(game, TestHelper.mockFileIO)
      controller.getPlayers.length should be >= 2
      controller.getServers.length should be > 0

      // Start the game
      if (controller.getState.currentPlayerIndex.isEmpty) {
        controller.doAndRemember(NextPlayerCommand())
      }
      controller.getState.currentPlayerIndex shouldBe Some(0)

      // Try to claim a server
      val firstServer = controller.getServers.head
      val claimCmd = ClaimServerCommand(firstServer.name, 0)
      controller.doAndRemember(claimCmd)

      // Verify server is claimed
      val claimedServer = controller.getServers.find(_.name == firstServer.name).get
      claimedServer.claimedBy shouldBe Some(0)

      // Test undo
      controller.canUndo shouldBe true
      controller.undo()
      val unclaimedServer = controller.getServers.find(_.name == firstServer.name).get
      unclaimedServer.claimedBy shouldBe None

      // Test redo
      controller.canRedo shouldBe true
      controller.redo()
      val reclaimedServer = controller.getServers.find(_.name == firstServer.name).get
      reclaimedServer.claimedBy shouldBe Some(0)
    }

    "progress through multiple players and rounds" in {
      val game = GameFactory("default")
      val controller = Controller(game, TestHelper.mockFileIO)
      val numPlayers = controller.getPlayers.length

      // Start game
      controller.doAndRemember(NextPlayerCommand())
      val startPlayer = controller.getState.currentPlayerIndex.get
      val initialRound = controller.getState.round

      // Advance to next player
      controller.doAndRemember(NextPlayerCommand())
      val secondPlayer = controller.getState.currentPlayerIndex.get
      secondPlayer should not be startPlayer

      // Advance through remaining players to complete one full cycle
      for (_ <- 2 until numPlayers) {
        controller.doAndRemember(NextPlayerCommand())
      }

      // One more advance should bring us back to start player with incremented round
      controller.doAndRemember(NextPlayerCommand())
      controller.getState.currentPlayerIndex.get shouldBe startPlayer
      controller.getState.round should be > initialRound
    }

    "handle game state transitions" in {
      val game = GameFactory("default")
      val controller = Controller(game, TestHelper.mockFileIO)

      // Change phase
      controller.setPhase(Phase.ExecutingTurn)
      controller.getState.phase shouldBe Phase.ExecutingTurn

      controller.setPhase(Phase.AwaitingInput)
      controller.getState.phase shouldBe Phase.AwaitingInput

      // Change status
      controller.setStatus(GameStatus.Paused)
      controller.getState.status shouldBe GameStatus.Paused

      controller.setStatus(GameStatus.Running)
      controller.getState.status shouldBe GameStatus.Running
    }

    "handle different server types" in {
      val tile = Tile(0, 0, Continent.Asia)
      val servers = List(
        Server("Bank", tile, 20, 3, 2, false, ServerType.Bank),
        Server("Cloud", tile, 15, 2, 2, false, ServerType.Cloud),
        Server("Military", tile, 40, 5, 5, false, ServerType.Military),
        Server("GKS", tile, 80, 0, 0, false, ServerType.GKS),
        Server("Side", tile, 5, 1, 1, false, ServerType.Side),
        Server("Private", tile, 10, 2, 2, false, ServerType.Private)
      )

      servers.foreach { server =>
        server.serverType should not be null
        server.name should not be empty
      }
    }

    "handle all continents" in {
      Continent.values.foreach { continent =>
        continent.short.length shouldBe 2
        continent.short should not be empty
      }
    }
  }
}

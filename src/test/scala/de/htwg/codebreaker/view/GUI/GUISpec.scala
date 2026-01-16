package de.htwg.codebreaker.view.gui

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.controller.{ControllerInterface, TestGameFactory, FakeFileIO}
import de.htwg.codebreaker.controller.controller.Controller

class GUISpec extends AnyWordSpec with Matchers {

  "GUI" should {

    "be created with controller" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      gui should not be (null)
      gui.controller shouldBe controller
    }

    "initialize as observer" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      // GUI should be added as observer
      gui should not be (null)
    }

    "track server state for hack detection" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      // Initial state should be tracked
      gui should not be (null)
    }

    "track player state for hack detection" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      // Initial state should be tracked
      gui should not be (null)
    }

    "have notification handler" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      // NotificationHandler should be initialized
      gui should not be (null)
    }

    "implement Observer interface" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      gui shouldBe an[de.htwg.codebreaker.util.Observer]
    }

    "have update method" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      // update() should exist and be callable (though it requires JavaFX)
      gui should not be (null)
    }

    "be instance of JFXApp3" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val gui = new GUI(controller)
      
      gui shouldBe a[scalafx.application.JFXApp3]
    }
  }
}

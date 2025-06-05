package de.htwg.codebreaker.util

class CommandManager:
  private var undoStack: List[Command] = Nil
  private var redoStack: List[Command] = Nil

  def doCommand(command: Command): Unit =
    command.execute()
    undoStack = command :: undoStack
    redoStack = Nil // nach neuem Befehl kann nicht mehr redo’t werden

  def undo(): Unit =
    undoStack match
      case cmd :: rest =>
        cmd.undo()
        undoStack = rest
        redoStack = cmd :: redoStack
      case Nil =>
        println("Nichts zum Rückgängig machen.")

  def redo(): Unit =
    redoStack match
      case cmd :: rest =>
        cmd.execute()
        redoStack = rest
        undoStack = cmd :: undoStack
      case Nil =>
        println("Nichts zum Wiederholen.")

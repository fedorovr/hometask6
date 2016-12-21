package FedorovRoman

import bloxorz.Direction
import bloxorz.Game
import bridges.*

// Your solution should live in this folder/package only (rename _SurnameName accordingly.)
// You may add as many subpackages as you need, but the function 'createGame' below should live in the root _SurnameName package.
// Please DON'T copy the interface 'Game' here.

// x - block; * - regular cell; . - light cell; S - start; T - target;
// A..Z - switches; a..z - open bridges, null - closed bridges; null - no cell

class GameImpl(board: String, bridgesInfo: BridgesInfo?) : Game {
    private val gameBoard: GameBoard = GameBoard(board, bridgesInfo)

    override val height: Int
        get() = gameBoard.height
    override val width: Int
        get() = gameBoard.width

    override fun getCellValue(i: Int, j: Int): Char? = gameBoard.getCellValue(i, j)

    override fun toString(): String = gameBoard.toString()

    override fun processMove(direction: Direction): Unit = gameBoard.moveBlock(direction)

    override fun hasWon(): Boolean = gameBoard.hasWon()

    override fun suggestMoves(): List<Direction>? = MoveSuggester(gameBoard).suggestMovesWithBridges()
}

fun createGame(board: String, bridgesInfo: BridgesInfo? = null): Game = GameImpl(board, bridgesInfo)


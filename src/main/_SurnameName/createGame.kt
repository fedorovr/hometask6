package _SurnameName

import bloxorz.Direction
import bloxorz.Game
import bridges.BridgesInfo

// Your solution should live in this folder/package only (rename _SurnameName accordingly.)
// You may add as many subpackages as you need, but the function 'createGame' below should live in the root _SurnameName package.
// Please DON'T copy the interface 'Game' here.

// x - block; * - regular cell; . - light cell; S - start; T - target;
// A..Z - switches; a..z - open bridges, null - closed bridges; null - no cell

enum class GameElement(val letter: Char) {
    START('S'),
    TARGET('T'),
    LIGHT_CELL('.'),
    REGULAR_CELL('*'),
    BLOCK('x');

    companion object {
        fun from(letter: Char): GameElement? = GameElement.values().firstOrNull { it.letter == letter }
    }
}

data class BlockPosition(val pos: Position, val pos2: Position?) {
    companion object {
        fun from(pos: Position): BlockPosition = BlockPosition(pos, null)
    }
}

data class Position(val row: Int, val col: Int)

class Block(var blockPos: BlockPosition) {
    fun isOnPosition(r: Int, c: Int): Boolean =
            (blockPos.pos.row == r && blockPos.pos.col == c) ||
                    (blockPos.pos2 != null && blockPos.pos2!!.row == r && blockPos.pos2!!.col == c)

    fun isStanding(): Boolean = blockPos.pos2 == null

    fun isStandingOnPosition(position: Position): Boolean =
            isStanding() && isOnPosition(position.row, position.col)

    fun setStandingOnPosition(position: Position) {
        blockPos = BlockPosition(position, null)
    }

    fun setOnPosition(blockPosition: BlockPosition) {
        blockPos = blockPosition
    }

    fun move(direction: Direction): Unit {
        if (isStanding()) {
            when (direction) {
                Direction.UP -> {
                    blockPos = BlockPosition(Position(blockPos.pos.row - 2, blockPos.pos.col),
                            Position(blockPos.pos.row - 1, blockPos.pos.col))
                }
                Direction.DOWN -> {
                    blockPos = BlockPosition(Position(blockPos.pos.row + 1, blockPos.pos.col),
                            Position(blockPos.pos.row + 2, blockPos.pos.col))
                }
                Direction.RIGHT -> {
                    blockPos = BlockPosition(Position(blockPos.pos.row, blockPos.pos.col + 1),
                            Position(blockPos.pos.row, blockPos.pos.col + 2))
                }
                Direction.LEFT -> {
                    blockPos = BlockPosition(Position(blockPos.pos.row, blockPos.pos.col - 2),
                            Position(blockPos.pos.row, blockPos.pos.col - 1))
                }
            }
        } else { // block lies
            val isHorizontal = blockPos.pos.row == blockPos.pos2!!.row
            if (isHorizontal) {
                when (direction) {
                    Direction.LEFT -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row, blockPos.pos.col - 1), null)
                    }
                    Direction.RIGHT -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row, blockPos.pos.col + 2), null)
                    }
                    Direction.UP -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row - 1, blockPos.pos.col),
                                Position(blockPos.pos2!!.row - 1, blockPos.pos2!!.col))
                    }
                    Direction.DOWN -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row + 1, blockPos.pos.col),
                                Position(blockPos.pos2!!.row + 1, blockPos.pos2!!.col))
                    }
                }
            } else { // has vertical orientation
                when (direction) {
                    Direction.LEFT -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row, blockPos.pos.col - 1),
                                Position(blockPos.pos2!!.row, blockPos.pos2!!.col - 1))
                    }
                    Direction.RIGHT -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row, blockPos.pos.col + 1),
                                Position(blockPos.pos2!!.row, blockPos.pos2!!.col + 1))
                    }
                    Direction.UP -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row - 1, blockPos.pos.col), null)
                    }
                    Direction.DOWN -> {
                        blockPos = BlockPosition(Position(blockPos.pos.row + 2, blockPos.pos.col), null)
                    }
                }
            }
        }
    }
}

class GameBoard(board: String) {
    private val gameBoard: MutableList<MutableList<GameElement?>>
    private val block: Block
    private val targetPosition: Position
    val startPosition: Position

    val height: Int
        get() = gameBoard.size
    val width: Int
        get() = gameBoard[0].size

    init {
        gameBoard = mutableListOf()
        val rows = board.split("\n")
        rows.forEach { row ->
            val currentRow: MutableList<GameElement?> = mutableListOf()
            gameBoard.add(currentRow)
            run {       // TODO: make it better
                row.filterIndexed { colIdx, _c -> colIdx % 2 == 0 }
                        .forEachIndexed { colIdx, letter -> currentRow.add(GameElement.from(letter)) }
            }
        }
        var startIdx: Position? = null
        var targetIdx: Position? = null
        for (rowIdx in 0..gameBoard.size - 1) {
            for (colIdx in 0..gameBoard[rowIdx].size - 1) {
                if (gameBoard[rowIdx][colIdx] == GameElement.START) {
                    startIdx = Position(rowIdx, colIdx)
                } else if (gameBoard[rowIdx][colIdx] == GameElement.TARGET) {
                    targetIdx = Position(rowIdx, colIdx)
                }
            }
        }
        if (startIdx == null) {
            // I'd prefer to throw an exception if targetIdx is null, but tests claims that it is possible to don't have a target
            throw IllegalArgumentException()
        }
        block = Block(BlockPosition(startIdx, null))
        startPosition = startIdx
        targetPosition = if (targetIdx == null) Position(-1, -1) else targetIdx
    }

    fun getCellValue(idx: Pair<Int, Int>): GameElement? = getCellValue(idx.first, idx.second)
    fun getCellValue(i: Int, j: Int): GameElement? =
            if (block.isOnPosition(i - 1, j - 1)) GameElement.BLOCK else gameBoard[i - 1][j - 1]

    fun setBlockPosition(blockPosition: BlockPosition): Unit {
        block.setOnPosition(blockPosition)
    }

    fun moveBlock(direction: Direction): Unit {
        block.move(direction)
        if (!isBlockOnLegalPosition()) {
            block.setStandingOnPosition(startPosition)
        }
    }

    private fun isBlockOnLegalPosition(): Boolean =
            try {
                // TODO: remove if
                gameBoard[block.blockPos.pos.row][block.blockPos.pos.col] != null &&
                        (if (block.blockPos.pos2 != null) gameBoard[block.blockPos.pos2!!.row][block.blockPos.pos2!!.col] != null else true)
            } catch (e: IndexOutOfBoundsException) {
                false
            }

    fun lastMoveWasSuccessful(): Boolean = !block.isStandingOnPosition(startPosition)

    fun hasWon(): Boolean = block.isStandingOnPosition(targetPosition)

    fun blockPosition(): BlockPosition = block.blockPos

    override fun toString(): String {
        return buildString {
            for (rowIdx in 0..gameBoard.size - 1) {
                for (colIdx in 0..gameBoard[rowIdx].size - 1) {
                    append(if (block.isOnPosition(rowIdx, colIdx))
                        GameElement.BLOCK.letter else gameBoard[rowIdx][colIdx]?.letter ?: ' ')
                    if (colIdx != gameBoard[rowIdx].size - 1) append(' ')
                }
                if (rowIdx != gameBoard.size - 1) append('\n')
            }
        }
    }
}

class GameImpl(board: String) : Game {
    private val gameBoard: GameBoard = GameBoard(board)

    override val height: Int
        get() = gameBoard.height
    override val width: Int
        get() = gameBoard.width

    override fun getCellValue(i: Int, j: Int): Char? = gameBoard.getCellValue(i, j)?.letter

    override fun toString(): String = gameBoard.toString()

    override fun processMove(direction: Direction): Unit = gameBoard.moveBlock(direction)

    override fun hasWon(): Boolean = gameBoard.hasWon()

    override fun suggestMoves(): List<Direction>? = MoveSuggester(gameBoard).suggestMoves()
}

fun createGame(board: String, bridgesInfo: BridgesInfo? = null): Game = GameImpl(board)

class MoveSuggester(val board: GameBoard) {
    fun suggestMoves(): List<Direction>? {
        val startBlockPosition = BlockPosition.from(board.startPosition)
        val queue: MutableList<BlockPosition> = mutableListOf(startBlockPosition)
        val pathFromStartToBlock: MutableMap<BlockPosition, MutableList<Direction>> =
                mutableMapOf(Pair(startBlockPosition, mutableListOf()))

        while (queue.isNotEmpty()) {
            val currentBlockPosition = queue.removeAt(0)
            for (direction in Direction.values()) {
                board.setBlockPosition(currentBlockPosition)
                board.moveBlock(direction)
                val newBlockPosition = board.blockPosition()
                if (board.lastMoveWasSuccessful() && pathFromStartToBlock[newBlockPosition] == null) {
                    pathFromStartToBlock[newBlockPosition] = pathFromStartToBlock[currentBlockPosition]!!
                            .toMutableList().apply { add(direction) }
                    queue.add(newBlockPosition)
                    if (board.hasWon()) {
                        board.setBlockPosition(startBlockPosition)
                        return pathFromStartToBlock[newBlockPosition]
                    }
                }
            }
        }
        board.setBlockPosition(startBlockPosition)
        return null
    }
}

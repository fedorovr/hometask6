package _SurnameName

import bloxorz.Direction
import bloxorz.Game
import bridges.*

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
    BLOCK('x'),
    SWITCH('_'),
    BRIDGE('_');

    companion object {
        private val SWITCH_LETTERS: CharRange = 'A'..'R'
        private val BRIDGE_LETTERS: CharRange = 'a'..'r'

        fun from(letter: Char): GameElement? =
                if (letter in SWITCH_LETTERS) {
                    SWITCH
                } else if (letter in BRIDGE_LETTERS) {
                    BRIDGE
                } else
                    GameElement.values().firstOrNull { it.letter == letter }
    }
}

data class BlockPosition(val pos: Position, val pos2: Position?)

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

class GameBoard(board: String, bridgesInfo: BridgesInfo?) {
    private val gameBoard: MutableList<MutableList<GameElement?>> = mutableListOf()
    private val block: Block
    private val targetPosition: Position
    private val bridgesPositions: MutableMap<Position, Bridge> = mutableMapOf()
    private val bridgesStates: MutableMap<Bridge, BridgeState> = mutableMapOf()
    private val switchersPositions: MutableMap<Position, Switch> = mutableMapOf()
    val startPosition: Position

    val height: Int
        get() = gameBoard.size
    val width: Int
        get() = gameBoard[0].size

    init {
        var startPos: Position? = null
        var targetPos: Position? = null

        board.split("\n").forEachIndexed { rowIdx, row ->
            gameBoard.add(
                    row
                            .filterIndexed { idx, _c -> idx % 2 == 0 }
                            .mapIndexed { colIdx, letter ->
                                val position = Position(rowIdx, colIdx)
                                GameElement.from(letter).apply {
                                    when (this) {
                                        GameElement.START -> startPos = position
                                        GameElement.TARGET -> targetPos = position
                                        GameElement.BRIDGE -> {
                                            val bridge: Bridge = bridgesInfo!!.bridges[letter]!!
                                            bridgesPositions[position] = bridge
                                            bridgesStates[bridge] = bridge.initialState
                                        }
                                        GameElement.SWITCH -> {
                                            switchersPositions[position] = bridgesInfo!!.switches[letter]!!
                                        }
                                    }
                                }
                            }
                            .toMutableList()
            )
        }
        if (startPos == null) {
            // I'd prefer to throw an exception if targetPos is null, but tests claims that it is possible to don't have a target
            throw IllegalArgumentException()
        }
        block = Block(BlockPosition(startPos!!, null))
        startPosition = startPos!!
        targetPosition = if (targetPos == null) Position(-1, -1) else targetPos!!
    }

    fun getCellValue(i: Int, j: Int): Char? =
            if (block.isOnPosition(i - 1, j - 1)) GameElement.BLOCK.letter else
                when (gameBoard[i - 1][j - 1]) {
                    GameElement.BRIDGE -> {
                        val bridge = bridgesPositions[Position(i - 1, j - 1)]
                        if (bridgesStates[bridge] == BridgeState.OPENED) bridge?.name else null
                    }
                    GameElement.SWITCH ->
                        switchersPositions[Position(i - 1, j - 1)]?.name
                    else ->
                        gameBoard[i - 1][j - 1]?.letter
                }

    fun setBlockOnPosition(blockPosition: BlockPosition): Unit {
        block.setOnPosition(blockPosition)
    }

    fun moveBlock(direction: Direction): Unit {
        block.move(direction)
        if (isBlockOnLegalPosition()) {
            handleSwitchers()
        } else {
            block.setStandingOnPosition(startPosition)
            bridgesStates.keys.forEach { bridgesStates[it] = it.initialState }
        }
    }

    private fun handleSwitchers(): Unit {
        val triggeredSwitchers = mutableListOf<Switch?>()
        if (block.isStanding() && switchersPositions[blockPosition().pos]?.type != null) {
            triggeredSwitchers.add(switchersPositions[blockPosition().pos])
        } else if (!block.isStanding()) {
            if (switchersPositions[blockPosition().pos]?.type == SwitchType.HALF_BLOCK)
                triggeredSwitchers.add(switchersPositions[blockPosition().pos])
            if (switchersPositions[blockPosition().pos2]?.type == SwitchType.HALF_BLOCK)
                triggeredSwitchers.add(switchersPositions[blockPosition().pos2])
        }
        triggeredSwitchers.filterNotNull().forEach {
            bridgesStates[it.bridge] = when (it.action) {
                SwitchAction.CHANGE ->
                    if (bridgesStates[it.bridge] == BridgeState.CLOSED) BridgeState.OPENED else BridgeState.CLOSED
                SwitchAction.CLOSE -> BridgeState.CLOSED
                SwitchAction.OPEN -> BridgeState.OPENED
            }
        }
    }

    private fun isBlockOnLegalPosition(): Boolean =
            try {
                val posLegal = gameBoard[blockPosition().pos.row][blockPosition().pos.col] != null
                val bridge = bridgesPositions[Position(blockPosition().pos.row, blockPosition().pos.col)]
                val posOnClosedBridge = bridge != null && bridgesStates[bridge] == BridgeState.CLOSED

                val pos2Legal = block.isStanding() || gameBoard[blockPosition().pos2!!.row][blockPosition().pos2!!.col] != null
                val pos2OnClosedBridge = if (!block.isStanding()) {
                    val bridge2 = bridgesPositions[Position(blockPosition().pos2!!.row, blockPosition().pos2!!.col)]
                    bridge2 != null && bridgesStates[bridge2] == BridgeState.CLOSED
                } else false

                val blockOnLightCell = block.isStanding() && gameBoard[blockPosition().pos.row][blockPosition().pos.col] == GameElement.LIGHT_CELL

                posLegal && !posOnClosedBridge && pos2Legal && !pos2OnClosedBridge && !blockOnLightCell
            } catch (e: IndexOutOfBoundsException) {
                false
            }

    fun lastMoveWasSuccessful(): Boolean = !block.isStandingOnPosition(startPosition)

    fun hasWon(): Boolean = block.isStandingOnPosition(targetPosition)

    fun blockPosition(): BlockPosition = block.blockPos

    override fun toString(): String {
        return buildString {
            for (rowIdx in 1..gameBoard.size) {
                for (colIdx in 1..gameBoard[rowIdx - 1].size) {
                    append(getCellValue(rowIdx, colIdx) ?: ' ')
                    if (colIdx != gameBoard[rowIdx - 1].size) append(' ')
                }
                if (rowIdx != gameBoard.size) append('\n')
            }
        }
    }
}

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

    override fun suggestMoves(): List<Direction>? = MoveSuggester(gameBoard).suggestMoves()
}

fun createGame(board: String, bridgesInfo: BridgesInfo? = null): Game = GameImpl(board, bridgesInfo)

class MoveSuggester(val board: GameBoard) {
    fun suggestMoves(): List<Direction>? {
        val startBlockPosition = board.blockPosition()
        val queue: MutableList<BlockPosition> = mutableListOf(startBlockPosition)
        val pathFromStartToBlock: MutableMap<BlockPosition, MutableList<Direction>> =
                mutableMapOf(Pair(startBlockPosition, mutableListOf()))

        while (queue.isNotEmpty()) {
            val currentBlockPosition = queue.removeAt(0)
            for (direction in Direction.values()) {
                board.setBlockOnPosition(currentBlockPosition)
                board.moveBlock(direction)
                val newBlockPosition = board.blockPosition()
                if (board.lastMoveWasSuccessful() && pathFromStartToBlock[newBlockPosition] == null) {
                    pathFromStartToBlock[newBlockPosition] = pathFromStartToBlock[currentBlockPosition]!!
                            .toMutableList().apply { add(direction) }
                    queue.add(newBlockPosition)
                    if (board.hasWon()) {
                        board.setBlockOnPosition(startBlockPosition)
                        return pathFromStartToBlock[newBlockPosition]
                    }
                }
            }
        }
        board.setBlockOnPosition(startBlockPosition)
        return null
    }
}

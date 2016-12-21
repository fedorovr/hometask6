package FedorovRoman

import bloxorz.Direction
import bridges.*

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

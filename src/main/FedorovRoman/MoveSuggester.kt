package FedorovRoman

import bloxorz.Direction
import bridges.Bridge
import bridges.BridgeState
import java.util.*

val OPENED_BRIDGE_CHAR = '0'
val CLOSED_BRIDGE_CHAR = '1'

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
                if (pathFromStartToBlock[newBlockPosition] == null) {
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

    data class BlockAndBridgesInfo(val blockPosition: BlockPosition, val compressedBridgesStates: String)

    private fun compressBridgesInfo(bridgesInfo : SortedMap<Bridge, BridgeState>): String =
            bridgesInfo.map { it -> if (it.value == BridgeState.OPENED) OPENED_BRIDGE_CHAR else CLOSED_BRIDGE_CHAR}
                    .toString()
                    .filter { it == OPENED_BRIDGE_CHAR || it == CLOSED_BRIDGE_CHAR }

    fun suggestMovesWithBridges(): List<Direction>? {
        val startBlockPosition = board.blockPosition()
        val startBridgesInfo = compressBridgesInfo(board.copySortedBridgesStates())
        val queue: MutableList<BlockAndBridgesInfo> =
                mutableListOf(BlockAndBridgesInfo(startBlockPosition, startBridgesInfo))
        val pathFromStartToBlock: MutableMap<BlockAndBridgesInfo, MutableList<Direction>> =
                mutableMapOf(Pair(BlockAndBridgesInfo(startBlockPosition, startBridgesInfo), mutableListOf()))

        while (queue.isNotEmpty()) {
            val currentBlockAndBridgesInfo: BlockAndBridgesInfo = queue.removeAt(0)
            for (direction in Direction.values()) {
                board.setBlockOnPosition(currentBlockAndBridgesInfo.blockPosition)
                board.setBridgesStates(currentBlockAndBridgesInfo.compressedBridgesStates)
                board.moveBlock(direction)
                val newBlockPosition = board.blockPosition()
                val newBridgesStates = board.copySortedBridgesStates()
                val newInfo = BlockAndBridgesInfo(newBlockPosition, compressBridgesInfo(newBridgesStates))

                if (pathFromStartToBlock[newInfo] == null) {
                    pathFromStartToBlock[newInfo] = pathFromStartToBlock[currentBlockAndBridgesInfo]!!
                            .toMutableList().apply { add(direction) }
                    queue.add(newInfo)
                    if (board.hasWon()) {
                        board.setBlockOnPosition(startBlockPosition)
                        board.setBridgesStates(startBridgesInfo)
                        return pathFromStartToBlock[newInfo]
                    }
                }
            }
        }
        board.setBlockOnPosition(startBlockPosition)
        return null
    }
}

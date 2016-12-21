package FedorovRoman

import bloxorz.Direction

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
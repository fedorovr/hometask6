package FedorovRoman

import bloxorz.Direction

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

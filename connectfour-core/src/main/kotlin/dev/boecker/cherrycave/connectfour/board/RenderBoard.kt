package dev.boecker.cherrycave.connectfour.board

import dev.boecker.cherrycave.connectfour.ConnectFourGame
import dev.boecker.cherrycave.connectfour.util.getItemStacks
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

fun ConnectFourBoard.renderBoard(itemSelection: ConnectFourGame.ConnectFourItemSelection, redPlayer: Player, bluePlayer: Player): Map<Int, ItemStack> {
    val (redItem, blueItem) = itemSelection.getItemStacks(redPlayer, bluePlayer)

    val itemMap = mutableMapOf<Int, ItemStack>()

    this.board.withIndex().forEach { (x, row) ->
        row.withIndex().forEach { (y, play) ->
            val itemSlot = findSlotFromPosition(x, y)
            when (play) {
                ConnectFourPlay.RedPlayer -> itemMap[itemSlot] = redItem
                ConnectFourPlay.BluePlayer -> itemMap[itemSlot] = blueItem
            }
        }
    }

    return itemMap
}

fun findSlotFromPosition(x: Int, y: Int): Int = ((5 - y) * 9) + x + 1
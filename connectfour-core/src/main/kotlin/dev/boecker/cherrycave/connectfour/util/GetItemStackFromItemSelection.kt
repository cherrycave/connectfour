package dev.boecker.cherrycave.connectfour.util

import dev.boecker.cherrycave.connectfour.ConnectFourGame
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

fun ConnectFourGame.ConnectFourItemSelection.getItemStacks(redPlayer: Player, bluePlayer: Player): Pair<ItemStack, ItemStack> {
    val redItem = ItemStack.of(this.redPlayer).with(DataComponents.CUSTOM_NAME, redPlayer.name)
    val blueItem = ItemStack.of(this.bluePlayer).with(DataComponents.CUSTOM_NAME, bluePlayer.name)

    return redItem to blueItem
}
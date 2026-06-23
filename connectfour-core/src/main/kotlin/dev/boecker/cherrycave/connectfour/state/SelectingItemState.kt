package dev.boecker.cherrycave.connectfour.state

import dev.boecker.cherrycave.connectfour.ConnectFourGame
import net.minestom.server.item.Material


class SelectingItemState(game: ConnectFourGame) : ConnectFourState(game) {

//    val redSelectionInventory: Inventory = Inventory(
//        InventoryType.CHEST_6_ROW, Component.text(
//            "Select Item",
//            NamedTextColor.BLUE
//        )
//    )

    init {
        // TODO: Item Selection Logic
        game.startGame(ConnectFourGame.ConnectFourItemSelection(
            Material.SLIME_BALL,
            Material.FIRE_CHARGE
        ))
    }

//    fun openSelectionInventory(player: Player) {
//
//    }
}
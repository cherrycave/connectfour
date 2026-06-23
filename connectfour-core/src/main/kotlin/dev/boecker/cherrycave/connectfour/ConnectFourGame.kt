package dev.boecker.cherrycave.connectfour

import dev.boecker.cherrycave.connectfour.board.ConnectFourBoard
import dev.boecker.cherrycave.connectfour.state.ConnectFourState
import dev.boecker.cherrycave.connectfour.state.IngameState
import dev.boecker.cherrycave.connectfour.state.SelectingItemState
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.item.Material

class ConnectFourGame(val manager: ConnectFourManager, val redPlayer: Player, val bluePlayer: Player, val eventNode: EventNode<Event>) {

    var gameState: ConnectFourState = SelectingItemState(this)

    data class ConnectFourItemSelection(
        val redPlayer: Material,
        val bluePlayer: Material
    )

    val board = ConnectFourBoard(
        MutableList(7) {
            mutableListOf()
        }
    )

    val players = listOf(redPlayer, bluePlayer)

    internal fun startGame(items: ConnectFourItemSelection) {
        MinecraftServer.getGlobalEventHandler().addChild(eventNode)

        gameState = IngameState(this, items)
    }

    internal fun finishGame() {

    }

}
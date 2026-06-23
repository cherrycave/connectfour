package dev.boecker.cherrycave.connectfour

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventNode

class ConnectFourManager {

    val games = ArrayList<ConnectFourGame>()

    fun createGame(redPlayer: Player, bluePlayer: Player) {
        if (games.any { game -> game.redPlayer.uuid == redPlayer.uuid || game.redPlayer.uuid == bluePlayer.uuid } || games.any { game -> game.bluePlayer.uuid == bluePlayer.uuid || game.bluePlayer.uuid == redPlayer.uuid }) {
            return
        }

        val game = ConnectFourGame(this, redPlayer, bluePlayer, EventNode.all("${redPlayer.username}-${bluePlayer.username}"))

        games.add(game)
    }

    fun finishGame(game: ConnectFourGame) {
        MinecraftServer.getGlobalEventHandler().removeChild(game.eventNode)

        games.removeIf {
            it.redPlayer.uuid == game.redPlayer.uuid && it.bluePlayer.uuid == game.bluePlayer.uuid
        }

        println(games)
    }

}
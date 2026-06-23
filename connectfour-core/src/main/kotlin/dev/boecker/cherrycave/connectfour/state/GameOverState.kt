package dev.boecker.cherrycave.connectfour.state

import dev.boecker.cherrycave.connectfour.ConnectFourGame
import dev.boecker.cherrycave.connectfour.board.ConnectFourPlay
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.component.DataComponents
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class GameOverState(
    val game: ConnectFourGame,
    val winner: ConnectFourPlay,
    giveUp: Boolean,
    val ingameState: IngameState
) :
    ConnectFourState(game) {

    init {
        game.eventNode.removeChild(ingameState.redNode)
        game.eventNode.removeChild(ingameState.blueNode)

        setWinnerColorInInventory()

        val winSound = Sound.sound(Key.key("entity.wither.death"), Sound.Source.AMBIENT, 0.5f, 1f)
        val loseSound = Sound.sound(Key.key("entity.elder_guardian.curse"), Sound.Source.AMBIENT, 0.5f, 1f)
        if (winner == ConnectFourPlay.RedPlayer) {
            game.redPlayer.playSound(winSound, Sound.Emitter.self())
            game.bluePlayer.playSound(loseSound, Sound.Emitter.self())
        } else {
            game.bluePlayer.playSound(winSound, Sound.Emitter.self())
            game.redPlayer.playSound(loseSound, Sound.Emitter.self())
        }

        if (giveUp) {
            game.players.forEach {
                it.closeInventory()
            }
            game.manager.finishGame(game)
        } else {
            game.eventNode.addListener(InventoryPreClickEvent::class.java) { event ->
                if (event.inventory == ingameState.redPlayerInventory || event.inventory == ingameState.bluePlayerInventory) {
                    event.isCancelled = true
                }
            }
            game.eventNode.addListener(InventoryCloseEvent::class.java) { event ->
                if (event.inventory == ingameState.redPlayerInventory) {
                    game.redPlayer.closeInventory()
                    game.bluePlayer.closeInventory()
                    game.manager.finishGame(game)
                } else if (event.inventory == ingameState.bluePlayerInventory) {
                    game.redPlayer.closeInventory()
                    game.bluePlayer.closeInventory()
                    game.manager.finishGame(game)
                }
            }
        }
    }

    fun setWinnerColorInInventory() {
        val redPlayerInventory = ingameState.redPlayerInventory
        val bluePlayerInventory = ingameState.bluePlayerInventory

        val winFrameItem = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
            .with(DataComponents.CUSTOM_NAME, Component.text("Winner", NamedTextColor.GREEN))
        val loseFrameItem = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
            .with(DataComponents.CUSTOM_NAME, Component.text("Loser", NamedTextColor.GREEN))

        for (i in 0 until InventoryType.CHEST_6_ROW.size step 9) {
            if (i / 9 != 2 && i / 9 != 3) {
                if (winner == ConnectFourPlay.RedPlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        winFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        loseFrameItem,
                    )
                } else if (winner == ConnectFourPlay.BluePlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        loseFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        winFrameItem,
                    )
                }
            }
        }
        for (i in 8 until InventoryType.CHEST_6_ROW.size step 9) {
            if (i / 8 != 3 && i / 8 != 4) {
                if (winner == ConnectFourPlay.RedPlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        loseFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        winFrameItem,
                    )
                } else if (winner == ConnectFourPlay.BluePlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        winFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        loseFrameItem,
                    )
                }
            }
        }
    }

}
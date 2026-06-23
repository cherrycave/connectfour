package dev.boecker.cherrycave.connectfour.state

import dev.boecker.cherrycave.connectfour.ConnectFourCoroutineScope
import dev.boecker.cherrycave.connectfour.ConnectFourGame
import dev.boecker.cherrycave.connectfour.board.ConnectFourPlay
import dev.boecker.cherrycave.connectfour.board.opposite
import dev.boecker.cherrycave.connectfour.util.getItemStacks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.component.DataComponents
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.trait.InventoryEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.EnchantmentList
import net.minestom.server.item.enchant.Enchantment
import net.minestom.server.network.player.ResolvableProfile
import kotlin.time.Duration.Companion.milliseconds

class IngameState(val game: ConnectFourGame, val itemSelection: ConnectFourGame.ConnectFourItemSelection) :
    ConnectFourState(game) {

    val redPlayerInventory = Inventory(
        InventoryType.CHEST_6_ROW,
        Component.join(
            JoinConfiguration.spaces(), Component.text("Connect Four", NamedTextColor.BLUE), Component.text(
                "vs",
                NamedTextColor.GRAY
            ), Component.text(game.bluePlayer.username, NamedTextColor.BLUE)
        )
    )

    val bluePlayerInventory = Inventory(
        InventoryType.CHEST_6_ROW,
        Component.join(
            JoinConfiguration.spaces(),
            Component.text("Connect Four", NamedTextColor.BLUE),
            Component.text("vs", NamedTextColor.GRAY),
            Component.text(game.redPlayer.username, NamedTextColor.RED)
        )
    )

    val redPlayerHead: ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).with(DataComponents.PROFILE, ResolvableProfile(game.redPlayer.skin)).with(
            DataComponents.CUSTOM_NAME, Component.text(game.redPlayer.username, NamedTextColor.RED)
        )

    val bluePlayerHead: ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).with(DataComponents.PROFILE, ResolvableProfile(game.bluePlayer.skin)).with(
            DataComponents.CUSTOM_NAME, Component.text(game.bluePlayer.username, NamedTextColor.BLUE)
        )

    val giveUpItem: ItemStack = ItemStack.of(Material.SPRUCE_DOOR)
        .with(DataComponents.CUSTOM_NAME, Component.text("Give Up", NamedTextColor.RED))

    private var turn: ConnectFourPlay? = ConnectFourPlay.entries.toTypedArray().random()
        set(value) {
            field = value

            updateInventoryTurnState()
        }

    val redNode: EventNode<InventoryEvent> = EventNode.type("click", EventFilter.INVENTORY) { _, inv ->
        redPlayerInventory == inv
    }

    val blueNode: EventNode<InventoryEvent> = EventNode.type("click", EventFilter.INVENTORY) { _, inv ->
        bluePlayerInventory == inv
    }

    val redItem: ItemStack
    val blueItem: ItemStack

    init {
        val (red, blue) = itemSelection.getItemStacks(game.redPlayer, game.bluePlayer)
        redItem = red
        blueItem = blue

        initializeFrame(redPlayerInventory)
        initializeFrame(bluePlayerInventory)

        initializeFrameTools()

        updateInventoryTurnState()

        redNode.addListener(InventoryPreClickEvent::class.java) { event: InventoryPreClickEvent ->
            event.isCancelled = true
            handleEventFor(ConnectFourPlay.RedPlayer, event)
        }
        redNode.addListener(InventoryCloseEvent::class.java) { event: InventoryCloseEvent ->
            if (event.isFromClient) {
                event.newInventory = redPlayerInventory
            }
        }

        blueNode.addListener(InventoryPreClickEvent::class.java) { event: InventoryPreClickEvent ->
            event.isCancelled = true

            handleEventFor(ConnectFourPlay.BluePlayer, event)
        }
        blueNode.addListener(InventoryCloseEvent::class.java) { event: InventoryCloseEvent ->
            if (event.isFromClient) {
                event.newInventory = bluePlayerInventory
            }
        }

        game.eventNode.addChild(redNode)
        game.eventNode.addChild(blueNode)

        game.redPlayer.openInventory(redPlayerInventory)
        game.bluePlayer.openInventory(bluePlayerInventory)
    }

    private fun initializeFrame(inventory: Inventory) {
        val frameItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)

        for (i in 0 until inventory.size) {
            if (i % 9 == 0 || i % 9 == 8) {
                inventory.setItemStack(
                    i,
                    frameItem,
                )
            }
        }
    }

    private fun initializeFrameTools() {
        redPlayerInventory.setItemStack(18, redPlayerHead)
        redPlayerInventory.setItemStack(27, getItemForPlay(ConnectFourPlay.RedPlayer))
        redPlayerInventory.setItemStack(26, bluePlayerHead)
        redPlayerInventory.setItemStack(35, getItemForPlay(ConnectFourPlay.BluePlayer))
        bluePlayerInventory.setItemStack(18, bluePlayerHead)
        bluePlayerInventory.setItemStack(27, getItemForPlay(ConnectFourPlay.BluePlayer))
        bluePlayerInventory.setItemStack(26, redPlayerHead)
        bluePlayerInventory.setItemStack(35, getItemForPlay(ConnectFourPlay.RedPlayer))

        redPlayerInventory.setItemStack(53, giveUpItem)
        bluePlayerInventory.setItemStack(53, giveUpItem)
    }

    private fun handleEventFor(handleTurn: ConnectFourPlay, event: InventoryPreClickEvent) {
        val slot = event.click.slot()

        if (slot > InventoryType.CHEST_6_ROW.size) {
            return
        }

        if (event.clickedItem == giveUpItem) {
            finishGame(handleTurn.opposite(), giveUp = true)
        }

        if (handleTurn != turn) {
            return
        }

        val inventoryColumn = slot % 9

        if (inventoryColumn in 1..7) {
            val x = inventoryColumn - 1
            val column = game.board.board[x]

            if (column.size >= 6) {
                return
            }

            play(handleTurn, x)
        }
    }

    fun updateInventoryTurnState() {
        val normalFrameItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)
        val turnFrameItem = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)

        for (i in 0 until InventoryType.CHEST_6_ROW.size step 9) {
            if (i / 9 != 2 && i / 9 != 3) {
                if (turn == ConnectFourPlay.RedPlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        turnFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                } else if (turn == ConnectFourPlay.BluePlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        turnFrameItem,
                    )
                } else {
                    redPlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                }
            }
        }
        for (i in 8 until InventoryType.CHEST_6_ROW.size step 9) {
            if (i / 8 != 3 && i / 8 != 4 && i / 8 != 6) {
                if (turn == ConnectFourPlay.RedPlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        turnFrameItem,
                    )
                } else if (turn == ConnectFourPlay.BluePlayer) {
                    redPlayerInventory.setItemStack(
                        i,
                        turnFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                } else {
                    redPlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                    bluePlayerInventory.setItemStack(
                        i,
                        normalFrameItem,
                    )
                }
            }
        }

        redPlayerInventory.update()
        bluePlayerInventory.update()

        val turnSound = Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 1f, 1f);
        if (turn == ConnectFourPlay.RedPlayer) {
            game.redPlayer.playSound(turnSound)
        } else if (turn == ConnectFourPlay.BluePlayer) {
            game.bluePlayer.playSound(turnSound)
        }
    }

    fun play(playerTurn: ConnectFourPlay, x: Int) {
        turn = null

        val column = game.board.board[x]

        ConnectFourCoroutineScope.launch {
            for (y in 5 downTo column.size) {
                findSlotFromPosition(x, y).let { slot ->
                    redPlayerInventory.setItemStack(slot, getItemForPlay(playerTurn))
                    bluePlayerInventory.setItemStack(slot, getItemForPlay(playerTurn))
                    redPlayerInventory.update()
                    bluePlayerInventory.update()
                    delay(100.milliseconds)
                    redPlayerInventory.setItemStack(slot, ItemStack.AIR)
                    bluePlayerInventory.setItemStack(slot, ItemStack.AIR)
                    redPlayerInventory.update()
                    bluePlayerInventory.update()
                }
            }

            column.addLast(playerTurn)

            updateInventoryBoardState()
            checkWin(x, column.size - 1, playerTurn)?.let {
                finishGame(playerTurn, it)
                return@launch
            }

            turn = if (playerTurn == ConnectFourPlay.RedPlayer) {
                ConnectFourPlay.BluePlayer
            } else {
                ConnectFourPlay.RedPlayer
            }
        }
    }

    fun updateInventoryBoardState() {
        val board = game.board.board

        for ((x, column) in board.withIndex()) {
            for ((y, play) in column.withIndex()) {
                val inventorySlot = findSlotFromPosition(x, y)

                redPlayerInventory.setItemStack(inventorySlot, getItemForPlay(play))
                bluePlayerInventory.setItemStack(inventorySlot, getItemForPlay(play))
            }
        }
        redPlayerInventory.update()
        bluePlayerInventory.update()
    }

    fun getItemForPlay(play: ConnectFourPlay): ItemStack {
        return if (play == ConnectFourPlay.RedPlayer) {
            redItem
        } else {
            blueItem
        }
    }

    fun findSlotFromPosition(x: Int, y: Int): Int = ((5 - y) * 9) + x + 1

    fun finishGame(winner: ConnectFourPlay, winningRow: List<Pair<Int, Int>>? = null, giveUp: Boolean = false) {
        turn = null
        winningRow?.let {
            it.forEach { (x, y) ->
                val slot = findSlotFromPosition(x, y)

                val enchantedRed = redPlayerInventory.getItemStack(slot).with(DataComponents.ENCHANTMENTS, EnchantmentList(Enchantment.UNBREAKING, 1))
                redPlayerInventory.setItemStack(slot, enchantedRed)
                val enchanted = bluePlayerInventory.getItemStack(slot).with(DataComponents.ENCHANTMENTS, EnchantmentList(Enchantment.UNBREAKING, 1))
                bluePlayerInventory.setItemStack(slot, enchanted)
            }
            redPlayerInventory.update()
            bluePlayerInventory.update()
        }

        game.gameState = GameOverState(game, winner, giveUp, this)
    }

    fun checkWin(x: Int, y: Int, play: ConnectFourPlay): List<Pair<Int, Int>>? {
        val board = game.board.board
        fun countInDirection(xStep: Int, yStep: Int): List<Pair<Int, Int>> {
            val count = mutableListOf<Pair<Int, Int>>()
            var x = x + xStep
            var y = y + yStep

            while (x in board.indices && y in board[x].indices && board[x][y] == play) {
                count.addLast(x to y)
                x += xStep
                y += yStep
            }
            return count
        }

        val left = countInDirection(-1, 0)
        val right = countInDirection(1, 0)
        if (1 + left.size + right.size >= 4) return left + (x to y) + right

        val down = countInDirection(0, -1)
        if (1 + down.size >= 4) return down + (x to y)

        val upLeft = countInDirection(1, -1)
        val downRight = countInDirection(-1, 1)
        if (1 + upLeft.size + downRight.size >= 4) return upLeft + (x to y) + downRight

        val downLeft = countInDirection(-1, -1)
        val upRight = countInDirection(1, 1)
        if (1 + downLeft.size + upRight.size >= 4) return downLeft + (x to y) + upRight

        return null
    }
}
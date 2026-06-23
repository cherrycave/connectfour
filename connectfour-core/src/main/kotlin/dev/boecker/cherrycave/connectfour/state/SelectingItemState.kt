package dev.boecker.cherrycave.connectfour.state

import dev.boecker.cherrycave.connectfour.ConnectFourGame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.trait.InventoryEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.TransactionOption
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.player.ResolvableProfile


class SelectingItemState(val game: ConnectFourGame) : ConnectFourState(game) {

    val redSelectionInventory: Inventory = Inventory(
        InventoryType.CHEST_6_ROW, Component.text(
            "Select Item",
            NamedTextColor.BLUE
        )
    )

    val blueSelectionInventory: Inventory = Inventory(
        InventoryType.CHEST_6_ROW, Component.text(
            "Select Item",
            NamedTextColor.BLUE
        )
    )

    var redSelection: Material? = null
        private set(value) {
            if (value == null) {
                return
            }
            field = value

            updateSelection(blueSelectionInventory, value, game.redPlayer)
            updateSelection(redSelectionInventory, value, game.redPlayer)
        }

    var blueSelection: Material? = null
        set(value) {
            if (value == null) {
                return
            }

            field = value

            updateSelection(redSelectionInventory, value, game.bluePlayer)
            updateSelection(blueSelectionInventory, value, game.bluePlayer)
        }

    val redNode: EventNode<InventoryEvent> = EventNode.type("red-select", EventFilter.INVENTORY) { _, inv ->
        redSelectionInventory == inv
    }

    val blueNode: EventNode<InventoryEvent> = EventNode.type("blue-select", EventFilter.INVENTORY) { _, inv ->
        blueSelectionInventory == inv
    }

    val itemMaterials = listOf(
        Material.SEA_LANTERN,
        Material.NETHERRACK,
        Material.GOLDEN_APPLE,
        Material.TNT,
        Material.WITHER_SKELETON_SKULL,
        Material.NETHERITE_INGOT,
        Material.ENDER_PEARL,
        Material.TOTEM_OF_UNDYING,
        Material.BELL,
        Material.SLIME_BALL,
        Material.FIRE_CHARGE,
        Material.POINTED_DRIPSTONE,
        Material.AMETHYST_SHARD,
        Material.NETHER_STAR,
        Material.LANTERN,
        Material.END_ROD,
        Material.CRYING_OBSIDIAN,
        Material.END_CRYSTAL,
        Material.MAGMA_BLOCK,
        Material.END_PORTAL_FRAME,
        Material.DRAGON_EGG,
        Material.DRAGON_HEAD,
        Material.CACTUS,
        Material.LILY_PAD,
        Material.SCULK_SENSOR,
        Material.BOOKSHELF,
        Material.SPYGLASS,
        Material.WIND_CHARGE,
        Material.ELYTRA,
    )

    init {
        fillSelectionInventory(redSelectionInventory)
        fillSelectionInventory(blueSelectionInventory)

        redNode.addListener(InventoryPreClickEvent::class.java) { event ->
            event.isCancelled = true

            if (redSelection == null && event.clickedItem.material() in itemMaterials) {
                redSelection = event.clickedItem.material()
            }
            startIfReady()
        }

        redNode.addListener(InventoryCloseEvent::class.java) { event ->
            event.newInventory = redSelectionInventory
        }

        blueNode.addListener(InventoryPreClickEvent::class.java) { event ->
            event.isCancelled = true

            if (blueSelection == null && event.clickedItem.material() in itemMaterials) {
                blueSelection = event.clickedItem.material()
            }
            startIfReady()
        }

        blueNode.addListener(InventoryCloseEvent::class.java) { event ->
            event.newInventory = blueSelectionInventory
        }

        game.eventNode.addChild(redNode)
        game.eventNode.addChild(blueNode)

        game.redPlayer.openInventory(redSelectionInventory)
        game.bluePlayer.openInventory(blueSelectionInventory)
    }

    private fun startIfReady() {
        if (redSelection == null || blueSelection == null) {
            return
        }

        game.eventNode.removeChild(redNode)
        game.eventNode.removeChild(blueNode)

        game.startGame(
            ConnectFourGame.ConnectFourItemSelection(
                redSelection!!,
                blueSelection!!,
            )
        )
    }

    fun fillSelectionInventory(inventory: Inventory) {
        val frameItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)

        for (i in 0 until InventoryType.CHEST_6_ROW.size) {
            if (i < 9 || i % 9 == 0 || i % 9 == 8 || i > (9 * 5)) {
                inventory.setItemStack(i, frameItem)
            }
        }

        inventory.addItemStacks(itemMaterials.map { ItemStack.of(it) }, TransactionOption.ALL)
    }

    fun updateSelection(inventory: Inventory, selection: Material, otherPlayer: Player) {
        val selectionSlot = inventory.itemStacks.indexOfFirst {
            it.material() == selection
        }

        inventory.setItemStack(
            selectionSlot, ItemStack.of(Material.PLAYER_HEAD).with(
                DataComponents.PROFILE,
                ResolvableProfile(otherPlayer.skin)
            ).with(DataComponents.CUSTOM_NAME, otherPlayer.name)
        )

        inventory.update()
    }
}
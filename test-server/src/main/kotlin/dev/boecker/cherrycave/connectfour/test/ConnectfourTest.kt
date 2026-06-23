package dev.boecker.cherrycave.connectfour.test

import dev.boecker.cherrycave.connectfour.ConnectFourManager
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.polar.PolarLoader
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText


class ConnectfourTest {
    private val logger = KotlinLogging.logger {}

    lateinit var connectFourGameManager: ConnectFourManager

    fun start() {
        System.setProperty("minestom.chunk-view-distance", "24");

        val auth = enableAuthentication()
        val minecraftServer = MinecraftServer.init(auth);

        val instanceManager = MinecraftServer.getInstanceManager()
        val instanceContainer = instanceManager.createInstanceContainer()

        instanceContainer.chunkLoader =
            PolarLoader(Path("worlds/world.polar"));

        connectFourGameManager = ConnectFourManager()

        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(
            AsyncPlayerConfigurationEvent::class.java
        ) { event: AsyncPlayerConfigurationEvent? ->
            val player: Player = event!!.player
            event.spawningInstance = instanceContainer
            player.respawnPoint = Pos(0.0, 0.0, 0.0)
            player.gameMode = GameMode.CREATIVE
        }

        globalEventHandler.addListener(
            PlayerEntityInteractEvent::class.java
        ) { event: PlayerEntityInteractEvent ->
            println("interact")
            val player: Player = event.player
            if (event.target is Player) {
                connectFourGameManager.createGame(player, event.target as Player)
            }
        }

        minecraftServer.start(System.getenv("HOST") ?: "[::1]", System.getenv("PORT")?.toInt() ?: 25565);
    }

    private fun enableAuthentication(): Auth {
        val path = Path("forwarding.secret")
        return if (path.exists()) {
            val secret = path.readText().trim()
            logger.info { "Enabling Velocity Auth: \"$secret\"" }
            Auth.Velocity(secret)
        } else {
            logger.info { "Enabling Mojang Auth" }
            Auth.Online()
        }
    }
}
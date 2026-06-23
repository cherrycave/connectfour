package dev.boecker.cherrycave.connectfour.board

enum class ConnectFourPlay {
    RedPlayer, BluePlayer
}

fun ConnectFourPlay.opposite(): ConnectFourPlay {
    return when (this) {
        ConnectFourPlay.BluePlayer -> ConnectFourPlay.RedPlayer
        ConnectFourPlay.RedPlayer -> ConnectFourPlay.BluePlayer
    }
}
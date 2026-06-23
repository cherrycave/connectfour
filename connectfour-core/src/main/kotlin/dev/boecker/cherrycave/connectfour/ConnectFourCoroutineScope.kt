package dev.boecker.cherrycave.connectfour

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

object ConnectFourCoroutineScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + Job()
}
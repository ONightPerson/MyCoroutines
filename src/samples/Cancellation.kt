package samples

import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun main() {
    runBlocking {
        cancelCheck()
    }
}

fun cancelCheck() = runBlocking {
    try {
        (1..5).asFlow().cancellable()
            .collect {
                if (it == 2) cancel()
                println("Collected $it")
            }

    } catch (e: CancellationException) {
        println("CancellationException $e")
    }
    println("End of block")

}
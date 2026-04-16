package samples

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    runBlocking {
        val request = launch {
            repeat(3) { i ->
                launch {
                    delay(((i + 1) * 200L).milliseconds)
                    println("Coroutine $i is done")
                }
            }
        }
        request.join()
        println("Now processing of the request is complete")
    }
}


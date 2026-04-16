package samples

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    runBlocking {
        log("Started main coroutine")
        val v1 = async( CoroutineName("v1Coroutine")) {
            delay(500.milliseconds)
            log("Computing in v1")
            6
        }

        val v2 = async(CoroutineName("v2Coroutine")) {
            delay(300.milliseconds)
            log("Computing in v2")
            4
        }
        println("sum of $v1 and $v2 is ${v1.await() + v2.await()}")
    }
}
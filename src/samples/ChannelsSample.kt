package samples

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.time.delay
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking<Unit> {
//    appyForLoop()
//    applyProduce()
    applyPingPong()
}

fun appyForLoop() = runBlocking<Unit> {
    val channel = Channel<Int>()
    launch {
        for (i in 1..5) {
            channel.send(i)
        }
        channel.close()
    }
    for (y in channel) {
        println("receive $y")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) {
        delay(1000.milliseconds)
        send(x * x)
    }
}

fun applyProduce() = runBlocking {
    val squares = produceSquares()
    squares.consumeEach { println(it) }
    println("Done!")
}

data class Ball(var hits: Int)

fun applyPingPong() = runBlocking {
    val table = Channel<Ball>() // a shared table
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0)) // serve the ball
    delay(1000.milliseconds) // delay 1 second
    coroutineContext.cancelChildren() // game over, cancel them
}

suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) { // receive the ball in a loop
        ball.hits++
        println("$name $ball")
        delay(300.milliseconds) // wait a bit
        table.send(ball) // send the ball back
    }
}

@OptIn(ObsoleteCoroutinesApi::class)
fun applyTicker() = runBlocking<Unit> {
    val tickerChannel = ticker(delayMillis = 200, initialDelayMillis = 0) // create a ticker channel
    var nextElement = withTimeoutOrNull(1.milliseconds) { tickerChannel.receive() }
    println("Initial element is available immediately: $nextElement") // no initial delay

    nextElement = withTimeoutOrNull(100.milliseconds) { tickerChannel.receive() } // all subsequent elements have 200ms delay
    println("Next element is not ready in 100 ms: $nextElement")

    nextElement = withTimeoutOrNull(120.milliseconds) { tickerChannel.receive() }
    println("Next element is ready in 200 ms: $nextElement")

    // Emulate large consumption delays
    println("Consumer pauses for 300ms")
    delay(300.milliseconds)
    // Next element is available immediately
    nextElement = withTimeoutOrNull(1.milliseconds) { tickerChannel.receive() }
    println("Next element is available immediately after large consumer delay: $nextElement")
    // Note that the pause between `receive` calls is taken into account and next element arrives faster
    nextElement = withTimeoutOrNull(120.milliseconds) { tickerChannel.receive() }
    println("Next element is ready in 100ms after consumer pause in 300ms: $nextElement")

    tickerChannel.cancel() // indicate that no more elements are needed
}

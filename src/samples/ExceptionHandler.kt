package samples

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds


fun main() = runBlocking {
//  testMultipleExceptions()
//    testCancellationException()
//    testSupervisorJob()
    testSupervisionScopes()
}

@OptIn(DelicateCoroutinesApi::class)
fun testMultipleExceptions() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            try {
                delay(Long.MAX_VALUE.milliseconds) // it gets cancelled when another sibling fails with IOException
            } finally {
                throw ArithmeticException() // the second exception
            }
        }
        launch {
            try {
                delay(500.milliseconds) // it gets cancelled when another sibling fails with IOException
            } finally {
                throw IllegalStateException() // the second exception
            }
        }
        launch {
            delay(100.milliseconds)
            throw IOException() // the first exception
        }
        delay(Long.MAX_VALUE.milliseconds)
    }
    job.join()
}

@OptIn(DelicateCoroutinesApi::class)
fun testCancellationException() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
    }
    val job = GlobalScope.launch(handler) {
        val innerJob = launch { // all this stack of coroutines will get cancelled
            launch {
                launch {
                    throw IOException() // the original exception
                }
            }
        }
        try {
            innerJob.join()
        } catch (e: CancellationException) {
            println("Rethrowing CancellationException with original cause")
            throw e // cancellation exception is rethrown, yet the original IOException gets to the handler
        }
    }
    job.join()
}

fun testSupervisorJob() = runBlocking {
    val supervisor = SupervisorJob()
    with(CoroutineScope(coroutineContext + supervisor)) {
        // launch the first child -- its exception is ignored for this example (don't do this in practice!)
        val firstChild = launch(CoroutineExceptionHandler { _, _ ->  }) {
            println("The first child is failing")
            throw AssertionError("The first child is cancelled")
        }
        // launch the second child
        val secondChild = launch {
            firstChild.join()
            // Cancellation of the first child is not propagated to the second child
            println("The first child is cancelled: ${firstChild.isCancelled}, but the second one is still active")
            try {
                delay(Long.MAX_VALUE.milliseconds)
            } finally {
                // But cancellation of the supervisor is propagated
                println("The second child is cancelled because the supervisor was cancelled")
            }
        }
        // wait until the first child fails & completes
        firstChild.join()
        println("Cancelling the supervisor")
        supervisor.cancel()
        secondChild.join()
    }
}

fun testSupervisionScopes() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    try {
        supervisorScope {
            val child = launch(handler) {
                println("child coroutine starts")
                delay(100.milliseconds)
                throw ArithmeticException()
            }
            val child2 = launch {
                println("child2 coroutine starts")
                delay(10000.milliseconds)
                println("child2 coroutine ends")
            }
            child.join()
            child2.join()
        }
    } catch (e: Throwable) {
        println("supervisorScope ends: $e")
    }
}


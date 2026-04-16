package samples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    runBlocking {
        applyFlatMapLatest()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun applyFlatMapLatest() = runBlocking {
    val startTime = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100.milliseconds) }
        .flatMapLatest { requestFlow(it) }
        .collect { println("$it at ${System.currentTimeMillis() - startTime} ms from start") }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun applyFlatMapMerge() = runBlocking {
    val startTime = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100.milliseconds) }
        .flatMapMerge { requestFlow(it) }
        .collect { println("$it at ${System.currentTimeMillis() - startTime} ms from start") }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun applyFlatMapConcat() = runBlocking {
    val startTime = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100.milliseconds) }
        .flatMapConcat { requestFlow(it) }
        .collect { println("$it at ${System.currentTimeMillis() - startTime} ms from start") }
}

fun applyOnEach() = runBlocking {
    val nums = (1..3).asFlow().onEach { delay(300.milliseconds) } // numbers 1..3 every 300 ms
    val strs = flowOf("one", "two", "three").onEach { delay(400.milliseconds) } // strings every 400 ms
    val startTime = System.currentTimeMillis() // remember the start time
    nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "zip"
        .collect { value -> // collect and print
            println("$value at ${System.currentTimeMillis() - startTime} ms from start")
        }
}

fun applyZip() = runBlocking {
    val nums: Flow<Number> = (1..3).asFlow()
    val strs: Flow<String> = flowOf("one", "two", "three")
    nums.zip(strs) {
        a, b -> "$a -> $b"
    }.collect { println(it) }
}

fun applyCollectLatest() = runBlocking {
    val time = measureTimeMillis {
        simple().collectLatest {
            println("Collecting $it")
            delay(300.milliseconds)
            println("Done collecting $it")

        }
    }
    println("Collected in $time ms")

}

fun applyConflate() = runBlocking {
    val time = measureTimeMillis {
        simple().conflate().collect {
            delay(300.milliseconds)
            println(it)
        }
    }
    println("Collected ${time}ms")
}

fun measureTime() = runBlocking {
    val time = measureTimeMillis {
        simple().collect { value ->
            delay(300.milliseconds)
            println(value)
        }
    }
    println("Collected in $time ms")
}

fun measureTimeWithBuffer() = runBlocking {
    val time = measureTimeMillis {
        simple().buffer().collect { value ->
            delay(300.milliseconds)
            println(value)
        }
    }
    println("Collected in $time ms")
}

fun applyFlowOn() = runBlocking<Unit> {
    simple()
        .flowOn(Dispatchers.Default)
        .collect { value -> log("Collect $value") }
}

suspend fun applyReduce() {
    val sum = (1..5).asFlow()
        .map { it * it }
        .reduce { a, b -> a * b }
    println("sum = $sum")
}

suspend fun applyTake() {
    numbers()
        .take(2)
        .collect { value -> println("response $value") }
}

suspend fun applyTransform() {
    (1..3).asFlow()
        .transform { i ->
            emit("This is $i request")
            emit(performRequest(i))
        }.collect { response -> println(response) }
}

suspend fun performRequest(request: Int): String {
    delay(1000.milliseconds) // imitate long-running asynchronous work
    return "response $request"
}

private fun numbers(): Flow<Int> = flow {
    try {
        emit(1)
        emit(2)
        println("This line will not execute")
        emit(3)
    } finally {
        println("Finally in numbers")
    }
}

private fun requestFlow(i: Int) :Flow<String> = flow {
    emit("$i: First")
    delay(500.milliseconds)
    emit("$i: Second")
}
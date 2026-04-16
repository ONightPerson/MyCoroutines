package samples

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
//    useTryCatch()
//    collectMistakeTryCatch()
    useCatch()
}

fun useCatch() = runBlocking {
    simple()
        .map {
            check(it <= 1) { "Value $it should be > 1" }
            "String $it"
        }
        .catch { t -> println("catch exception: $t") }
        .collect {
            println("Collected $it")
        }
}

fun useTryCatch() = runBlocking<Unit> {
    try {
        simple()
            .map {
                check(it <= 1) { "Value $it should be > 1" }
                "String $it"
            }
            .collect {
                println("Collected $it")
            }
    } catch (e: Throwable) {
        println("Catch exception $e")
    }
}

/**
 * 违反异常透明性
 * emit是一个挂起函数，它会将数据发送给下游的收集器。当收集器的代码抛出异常时，emit会直接抛出这个异常
 * 因为下游的异常会通过协程的取消/失败机制向上传递
 */
fun collectMistakeTryCatch() = runBlocking {
    mistakeTryCatch().collect {
        throw RuntimeException("Mistake exception $it")
    }
}

fun mistakeTryCatch(): Flow<Int> = flow {
    try {
        emit(1)
        emit(2)
    } catch (e: Exception) {
        println("Caught inside flow: $e")
    }
}
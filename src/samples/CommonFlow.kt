package samples

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i) // emit next value
    }
}
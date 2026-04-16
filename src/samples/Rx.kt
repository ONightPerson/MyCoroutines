package samples

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.rx3.asObservable

fun main() {
    // 将 Kotlin Flow 转换为 RxJava 3 的 Observable
    val observable: Observable<Int> = flowOf(1, 2, 3).asObservable()

    observable.subscribe { println("RxJava 3 received: $it") }
}
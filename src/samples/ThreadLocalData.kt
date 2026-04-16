package samples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

val threadLocal = ThreadLocal<String?>()

fun main() {
    runBlocking {
        threadLocal.set("main")
        println("Pre-main, current thread is ${Thread.currentThread()}, thread local data is ${threadLocal.get()}")

        val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
            println("Launch start, current thread is ${Thread.currentThread()}, thread local data is ${threadLocal.get()}")
            yield()
            println("Launch end, current thread is ${Thread.currentThread()}, thread local data is ${threadLocal.get()}")
        }
        job.join()
        println("Post-main, current thread is ${Thread.currentThread()}, thread local data is ${threadLocal.get()}")
    }
}
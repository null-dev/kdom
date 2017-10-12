package xyz.nulldev.kdom.api.util

import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

suspend fun XMLHttpRequest.await(): XMLHttpRequest
        = suspendCoroutine { cont ->
    onreadystatechange = {
        if (readyState.toInt() == 4) {
            cont.resume(this)
            status
        }
    }
}

fun async(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        override fun resume(value: Unit) {}
        override fun resumeWithException(e: Throwable) { console.log("Method threw exception!", e) }
    })
}

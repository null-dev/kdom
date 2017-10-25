package xyz.nulldev.kdom.api.util

import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

/**
 * Wait until a promise emits an item
 */
suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

/**
 * Wait until an HTTP request is complete
 */
suspend fun XMLHttpRequest.await(): XMLHttpRequest
        = suspendCoroutine { cont ->
    onreadystatechange = {
        if (readyState.toInt() == 4) {
            cont.resume(this)
            status
        }
    }
}

/**
 * Start a coroutine
 */
fun async(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        override fun resume(value: Unit) {}
        override fun resumeWithException(exception: Throwable) {
            console.error("A coroutine in the application threw an exception: ${exception.message}")
            console.error("Exception object:", exception)
        }
    })
}

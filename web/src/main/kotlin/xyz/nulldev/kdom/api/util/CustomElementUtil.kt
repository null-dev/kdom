package xyz.nulldev.kdom.api.util

import org.w3c.dom.EventInit
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.dom.set

private const val DATA_VALUE_KEY = "value"

var HTMLElement.dataValue: String
    get() = dataset[DATA_VALUE_KEY] ?: ""
    set(value) { dataset[DATA_VALUE_KEY] = value }

fun HTMLElement.on(type: String, listener: (Event) -> Unit) {
    addEventListener(type, listener)
}

fun HTMLElement.emit(type: String,
                     bubbles: Boolean = true,
                     cancelable: Boolean = false,
                     composed: Boolean = false) {
    dispatchEvent(Event(type, EventInit(bubbles, cancelable, composed)))
}
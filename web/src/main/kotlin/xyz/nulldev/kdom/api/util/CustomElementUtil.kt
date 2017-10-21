package xyz.nulldev.kdom.api.util

import org.w3c.dom.EventInit
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.dom.set
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.Element

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

fun <T : Component> HTMLElement.component(): T {
    return (asDynamic()[Component.COMPONENT_KEY]
            ?: throw IllegalArgumentException("This element is not a component!")) as T
}

fun <T : Component> Element<*>.component(): T {
    return value.component()
}

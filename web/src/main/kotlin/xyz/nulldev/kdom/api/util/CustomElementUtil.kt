package xyz.nulldev.kdom.api.util

import org.w3c.dom.EventInit
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.dom.set
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.Element

private const val DATA_VALUE_KEY = "value"

/**
 * Alias to the data["value"] field
 */
var HTMLElement.dataValue: String
    get() = dataset[DATA_VALUE_KEY] ?: ""
    set(value) { dataset[DATA_VALUE_KEY] = value }

/**
 * Alias to `addEventListener`
 */
fun HTMLElement.on(type: String, listener: (Event) -> Unit) {
    addEventListener(type, listener)
}

fun HTMLElement.emit(type: String,
                     bubbles: Boolean = true,
                     cancelable: Boolean = false,
                     composed: Boolean = false) {
    dispatchEvent(Event(type, EventInit(bubbles, cancelable, composed)))
}

/**
 * Get an HTMLElement's component
 * @throws IllegalArgumentException If this element is not the root element of any component
 */
fun <T : Component> HTMLElement.component(): T {
    return (asDynamic()[Component.COMPONENT_KEY]
            ?: throw IllegalArgumentException("This element is not a component!")) as T
}

/**
 * Get an HTMLElement's component
 * @throws IllegalArgumentException If this element is not the root element of any component
 */
fun <T : Component> Element<*>.component(): T {
    return value.component()
}

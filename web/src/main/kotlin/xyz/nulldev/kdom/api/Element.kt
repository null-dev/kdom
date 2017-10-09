package xyz.nulldev.kdom.api

import org.w3c.dom.HTMLElement
import xyz.nulldev.kdom.util.HUGE_STRING

class Element<T : HTMLElement>(val id: Long,
                               private val parent: Component): ValueStore<T> {
    override var value: T
        set(v) {
            throw IllegalArgumentException("The value of an element reference cannot be changed!")
        }
        get() {
            parent.compiledDom //Forcibly compile DOM
            return current
        }

    private lateinit var current: T

    internal fun setVal(value: T) {
        this.current = value
    }

    override fun toString() = "$HUGE_STRING-kdom-element_$id-kdom-$HUGE_STRING"
}
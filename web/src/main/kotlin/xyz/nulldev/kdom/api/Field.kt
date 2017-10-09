package xyz.nulldev.kdom.api

import xyz.nulldev.kdom.util.HUGE_STRING

open class Field<T : Any> internal constructor(val id: Long,
                                    initialVal: T,
                                    onUpdate: (Field<T>) -> Unit): ValueStore<T>, Updatable {
    override var value = initialVal
        set(value) {
            field = value
            update()
        }

    internal val updateListeners = mutableListOf(onUpdate)

    override fun update() {
        updateListeners.forEach {  it(this) }
    }

    override fun toString() = "$HUGE_STRING-kdom-field_$id-kdom-$HUGE_STRING"

    override fun equals(other: Any?): Boolean {
        return other != null && other is Field<*> && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
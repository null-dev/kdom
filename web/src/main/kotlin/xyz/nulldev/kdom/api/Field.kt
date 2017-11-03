package xyz.nulldev.kdom.api

import xyz.nulldev.kdom.util.HUGE_STRING

open class Field<T : Any> internal constructor(val id: Long, initialVal: T):
        ValueStore<T>, Updatable {
    internal var internalValue = initialVal

    override var value
        get() = internalValue
        set(value) {
            internalValue = value
            update()
        }

    override val updateListeners = mutableListOf<UpdateListener>()
    internal val parentComponents = mutableListOf<Component>()
    internal val transformListeners = mutableListOf<TransformedField<T, *>>()

    override fun update() {
        //Fire update listeners
        updateListeners.forEach {  it(this) }

        //Update parent components
        parentComponents.forEach {
            //Update mappings when variable changes
            it.compiledDom.mappings.forEach {
                if (it.fields.contains(this))
                    it.update()
            }
        }

        //Update transformed copies
        transformListeners.forEach {
            //Forcibly re-transform original values
            it.setAndTransformValue(value)
        }
    }

    override fun addUpdateListener(listener: UpdateListener) {
        updateListeners += listener
    }

    override fun removeUpdateListener(listener: UpdateListener) {
        updateListeners -= listener
    }

    override fun clearUpdateListeners() {
        updateListeners.clear()
    }

    fun <R : Any> transform(transformer: (T) -> R): TransformedField<T, R> {
        val field = TransformedField(Component.nextId(), this, transformer)

        //Import new field into all parent components
        parentComponents.forEach {
            it.internalImportRelay(field)
        }

        return field
    }

    override fun toString() = "$HUGE_STRING-kdom-field_$id-kdom-$HUGE_STRING"

    override fun equals(other: Any?): Boolean {
        return other != null && other is Field<*> && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
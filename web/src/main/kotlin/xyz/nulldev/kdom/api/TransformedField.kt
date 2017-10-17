package xyz.nulldev.kdom.api

class TransformedField<T : Any, R : Any>(id: Long,
                                         val parent: Field<T>,
                                         val transformer: (T) -> R): Field<R>(id, transformer(parent.value)) {
    init {
        //Hook into parent field
        parent.transformListeners += this
    }

    override var value
        get() = super.value
        set(value) {
            throw UnsupportedOperationException("The values of transformed fields cannot be directly modified!")
        }

    internal fun setAndTransformValue(value: T) {
        super.value = transformer(value)
    }
}
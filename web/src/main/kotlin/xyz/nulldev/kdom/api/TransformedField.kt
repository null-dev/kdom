package xyz.nulldev.kdom.api

class TransformedField<T : Any, R : Any>(id: Long,
                                         val parent: Field<T>,
                                         val transformer: (T) -> R): ReadOnlyField<R>(id, transformer(parent.value)) {
    init {
        //Hook into parent field
        parent.transformListeners += this
    }

    internal fun setAndTransformValue(value: T) {
        super.forceSetValue(transformer(value))
    }
}
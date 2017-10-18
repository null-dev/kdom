package xyz.nulldev.kdom.api

class ReadOnlyField<T : Any>(id: Long, initialValue: T): Field<T>(id, initialValue) {
    override var value: T
        get() = super.value
        set(value) {
            throw UnsupportedOperationException("This field is read only!")
        }

    internal fun forceSetValue(value: T) {
        super.value = value
    }
}
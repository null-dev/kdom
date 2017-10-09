package xyz.nulldev.kdom.api

interface ValueStore<T> {
    var value: T
    var v: T
        get() = value
        set(v) { value = v }

    operator fun invoke() = value
    operator fun invoke(value: T) {
        this.value = value
    }
}
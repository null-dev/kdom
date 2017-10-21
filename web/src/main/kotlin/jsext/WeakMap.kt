package jsext

external class WeakMap<K, V> {
    fun set(key: K, value: V): WeakMap<K, V> = definedExternally
    fun get(key: K): V? = definedExternally
    fun has(key: K): Boolean = definedExternally
    fun delete(key: K): Boolean = definedExternally
}
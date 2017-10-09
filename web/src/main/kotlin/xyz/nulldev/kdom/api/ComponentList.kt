package xyz.nulldev.kdom.api

class ComponentList<T : Component>(val id: Long,
                                   onUpdate: (ComponentList<T>) -> Unit,
                                   private val internalList: MutableList<T> = mutableListOf()):
        List<T> by internalList, AbstractMutableList<T>(), Updatable {
    internal val internalField = Field(id, internalList, {
        onUpdate(this)
    })

    override fun update() {
        updateListeners.forEach { it(this) }
    }

    internal val updateListeners = mutableListOf(onUpdate)

    //Delegated mutation observed methods
    override fun add(index: Int, element: T) {
        internalList.add(index, element)
        update()
    }

    override fun removeAt(index: Int): T {
        return internalList.removeAt(index).apply {
            update()
        }
    }

    override fun set(index: Int, element: T): T {
        return internalList.set(index, element).apply {
            update()
        }
    }

    //Explicit overrides
    override fun iterator() = super.iterator()
    override fun listIterator() = super.listIterator()
    override fun listIterator(index: Int) = super.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int) = super.subList(fromIndex, toIndex)
    override fun contains(element: T) = internalList.contains(element)
    override fun containsAll(elements: Collection<T>) = internalList.containsAll(elements)
    override fun indexOf(element: T) = internalList.indexOf(element)
    override fun isEmpty() = internalList.isEmpty()
    override fun lastIndexOf(element: T) = internalList.lastIndexOf(element)

    // Object methods
    override fun toString() = internalField.toString()
    override fun equals(other: Any?) = internalField.equals(other)
    override fun hashCode() = internalField.hashCode()
}

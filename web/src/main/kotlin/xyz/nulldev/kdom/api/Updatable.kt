package xyz.nulldev.kdom.api

interface Updatable {
    fun update()

    val updateListeners: List<UpdateListener>

    fun addUpdateListener(listener: UpdateListener)
    fun removeUpdateListener(listener: UpdateListener)
    fun clearUpdateListeners()
}

typealias UpdateListener = (Updatable) -> Unit
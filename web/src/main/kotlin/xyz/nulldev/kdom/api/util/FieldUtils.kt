package xyz.nulldev.kdom.api.util

import xyz.nulldev.kdom.api.Field

/**
 * Bind one field to another
 *
 * @param field The master field, if updates on this field's value will be propagated to the receiver
 * @receiver The slave field, updates when the master field updates
 */
fun <T : Any> Field<T>.from(field: Field<T>?): Field<T> {
    field?.let {
        it.addUpdateListener {
            value = field.value
        }
        internalValue = it.value
    }
    return this
}

/**
 * Shortcut to cast one field type to another
 */
fun <T : Any> Field<*>.cast(): Field<T> {
    return this as Field<T>
}
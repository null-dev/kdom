package xyz.nulldev.kdom.api.util

import org.w3c.xhr.XMLHttpRequest

/**
 * Whether or not the status code of this request is 200
 */
val XMLHttpRequest.isSuccessful
    get() = status.toInt() == 200
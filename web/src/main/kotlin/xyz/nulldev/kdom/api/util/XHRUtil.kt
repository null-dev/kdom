package xyz.nulldev.kdom.api.util

import org.w3c.xhr.XMLHttpRequest

val XMLHttpRequest.isSuccessful
    get() = status.toInt() == 200
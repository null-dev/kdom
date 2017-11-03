package xyz.nulldev.kdom.mdcshim

fun String.toBoolean(): Boolean {
    val trimmed = trim()
    return if(trimmed.equals("true", true)) {
        true
    } else if(trimmed.equals("false", true)) {
        false
    } else {
        throw IllegalArgumentException("Invalid boolean: $this")
    }
}
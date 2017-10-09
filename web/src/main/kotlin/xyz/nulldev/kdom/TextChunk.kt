package xyz.nulldev.kdom

sealed class TextChunk {
    data class Text(val value: String): TextChunk()
    data class Field(val field: xyz.nulldev.kdom.api.Field<out Any>): TextChunk()
}
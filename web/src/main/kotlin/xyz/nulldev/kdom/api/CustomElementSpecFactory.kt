package xyz.nulldev.kdom.api

interface CustomElementSpecFactory {
    val tag: String
    fun generate(request: CustomElementRequest): Component

    val spec
        get() = CustomElementSpec(tag, { generate(it) })
}
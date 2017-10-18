package xyz.nulldev.kdom.api

object SpecManager {
    private val registeredSpecs = mutableMapOf<String, CustomElementSpec>()

    fun registerSpec(spec: CustomElementSpec) {
        spec.validate()
        registeredSpecs.put(cleanTagName(spec.tagName), spec)
    }

    internal fun specFor(tagName: String) = registeredSpecs[cleanTagName(tagName)]

    private fun cleanTagName(tagName: String) = tagName.trim().toLowerCase()
}
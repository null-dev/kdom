package xyz.nulldev.kdom.api

import xyz.nulldev.kdom.api.compat.KTableSpec
import xyz.nulldev.kdom.api.compat.KTdSpec
import xyz.nulldev.kdom.api.compat.KThSpec
import xyz.nulldev.kdom.api.compat.KTrSpec

object SpecManager {
    private val registeredSpecs = mutableMapOf<String, CustomElementSpec>()

    fun registerSpec(spec: CustomElementSpec) {
        spec.validate()
        registeredSpecs.put(cleanTagName(spec.tagName), spec)
    }

    fun registerFactory(factory: CustomElementSpecFactory)
        = registerSpec(factory.spec)

    internal fun specFor(tagName: String) = registeredSpecs[cleanTagName(tagName)]

    private fun cleanTagName(tagName: String) = tagName.trim().toLowerCase()

    init {
        //Register internal specs
        listOf(
                KTableSpec,
                KTrSpec,
                KTdSpec,
                KThSpec
        ).forEach { registerFactory(it) }
    }
}
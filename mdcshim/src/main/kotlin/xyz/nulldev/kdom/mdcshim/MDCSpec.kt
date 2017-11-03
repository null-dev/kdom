package xyz.nulldev.kdom.mdcshim

import xyz.nulldev.kdom.api.SpecManager
import xyz.nulldev.kdom.api.util.specTo

class MDCSpec {
    companion object {
        val specs = listOf(
                "mdc-button" specTo { MDCButton(it) }
        )

        fun register() {
            specs.forEach {
                SpecManager.registerSpec(it)
            }
        }
    }
}

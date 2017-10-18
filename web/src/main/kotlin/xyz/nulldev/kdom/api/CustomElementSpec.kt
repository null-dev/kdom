package xyz.nulldev.kdom.api

class CustomElementSpec(val tagName: String,
                        val spec: (CustomElementRequest) -> Component) {
    fun validate() {
        if(!tagName.contains('-')) {
            throw IllegalArgumentException("Illegal tag name! Tag name must contain a dash (-)!")
        }
    }
}
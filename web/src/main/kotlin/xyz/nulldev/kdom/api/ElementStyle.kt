package xyz.nulldev.kdom.api

import azadev.kotlin.css.Stylesheet
import xyz.nulldev.kdom.util.HUGE_STRING

class ElementStyle(content: String) {
    private val generated by lazy {
        content.replace("\"", "&#34;") // Escape double quote
                .replace("'", "&#39;") // Escape single quote
                .replace("\n", "&#10;") // Escape line feed
                .replace("\r", "&#13;") // Escape carriage return
    }

    constructor(gen: Stylesheet.() -> Unit):
            this(
                    Stylesheet {
                        any.attr("$PLACEHOLDER_STYLE_KEY=$PLACEHOLDER_STYLE_VALUE") {
                            gen(this)
                        }
                    }.render()
            )

    override fun toString() = generated

    companion object {
        internal val PLACEHOLDER_STYLE_KEY = "kdom-$HUGE_STRING-kstyle"
        internal val PLACEHOLDER_STYLE_VALUE = "kdom-$HUGE_STRING-placeholder-style"
    }
}
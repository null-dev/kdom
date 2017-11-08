package xyz.nulldev.kdom

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.Text
import xyz.nulldev.kdom.api.ElementStyle
import xyz.nulldev.kdom.util.HUGE_STRING
import kotlin.browser.document

object StyleManager {
    private var lastId = 0
    private val styleRefs = mutableMapOf<String, StyleEntry>()

    fun nextIdClass()
        = "kdom-${HUGE_STRING}-kstyle-${lastId++}"

    fun createStyle(clazz: String, nodes: List<Text>): Pair<String, Element> {
        val styleElement = document.createElement("style")
        // Append style nodes to element
        nodes.forEach {
            styleElement.appendChild(it)
        }
        return Pair(clazz, styleElement)
    }

    fun attachStyle(style: String): String {
        val orig = styleRefs[style]
        return if(orig == null) {
            //Attach new style if not in cache
            val clazz = nextIdClass()
            val newStyle = style.replace(ElementStyle.PLACEHOLDER_STYLE_VALUE, clazz)
            val styleElement = document.createElement("style") as HTMLStyleElement
            styleElement.appendChild(document.createTextNode(newStyle))
            document.head?.appendChild(styleElement) ?: throw IllegalStateException("Document head not found!")
            styleRefs.put(style, StyleEntry(styleElement, clazz))
            clazz
        } else {
            //Increment reference counter
            orig.refs++
            orig.clazz
        }
    }

    fun detachStyle(style: String) {
        val orig = styleRefs[style]
                ?: throw IllegalStateException("An attempt was made to detach a style that was not attached!")

        //Remove and detach style if no references to id
        if(--orig.refs <= 0) {
            styleRefs -= style
            orig.element.remove()
        }
    }
}

private class StyleEntry(val element: HTMLStyleElement,
                         val clazz: String,
                         var refs: Long = 1)
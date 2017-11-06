package xyz.nulldev.kdom

import org.w3c.dom.Element
import org.w3c.dom.Text
import xyz.nulldev.kdom.util.HUGE_STRING
import kotlin.browser.document

object StyleManager {
    var lastId = 0

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
}
package xyz.nulldev.kdom

import org.w3c.dom.*
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.ComponentList
import xyz.nulldev.kdom.api.Field
import kotlin.browser.document

sealed class DomMapping(val fields: List<Field<out Any>>) {
    abstract fun update()

    class TextMapping(private val text: Text,
                      private val field: Field<out Any>): DomMapping(listOf(field)) {
        override fun update() {
            text.textContent = field.value.toString()
        }
    }
    class ComponentMapping(private var node: Node,
                           private val field: Field<out Component>): DomMapping(listOf(field)) {
        override fun update() {
            node.parentNode!!.replaceChild(field.value.compiledDom.root, node)
            node = field.value.compiledDom.root
        }
    }
    class ComponentListMapping(private val oldState: MutableList<Node>,
                               private val list: ComponentList<out Component>): DomMapping(listOf(list.internalField)) {
        override fun update() {
            //Get parent
            val parent = oldState[0].parentNode!!
            //Insert dummy node
            val dummy = document.createTextNode("")
            parent.insertBefore(dummy, oldState[0])
            //Remove all old nodes
            oldState.forEach { parent.removeChild(it) }
            //Clear old state
            oldState.clear()
            //Insert new nodes and save to state
            list.forEach {
                val cdom = it.compiledDom.root
                oldState.add(cdom)
                parent.insertBefore(cdom, dummy)
            }
            //Save dummy node to state
            oldState.add(dummy)
        }
    }
    class AttributeMapping(private val attr: Attr,
                           private val chunks: List<TextChunk>):
            DomMapping(chunks
                    .filterIsInstance<TextChunk.Field>()
                    .map(TextChunk.Field::field)) {
        override fun update() {
            attr.value = chunks.joinToString(separator = "") {
                when (it) {
                    is TextChunk.Text -> it.value
                    is TextChunk.Field -> it.field.value.toString()
                }
            }
        }
    }
}
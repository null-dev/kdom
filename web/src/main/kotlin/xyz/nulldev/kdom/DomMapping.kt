package xyz.nulldev.kdom

import org.w3c.dom.*
import xyz.nulldev.kdom.api.*
import kotlin.browser.document

sealed class DomMapping(val fields: List<Field<out Any>>) {
    abstract fun update()

    protected fun MutableList<Node>.replaceWith(newNodes: List<Node>) {
        //Get parent
        val parent = this[0].parentNode!!
        //Insert dummy node
        val dummy = document.createTextNode("")
        parent.insertBefore(dummy, this[0])
        //Remove all old nodes
        this.forEach { parent.removeChild(it) }
        //Clear old state
        this.clear()
        //Insert new nodes and save to state
        newNodes.forEach {
            this.add(it)
            parent.insertBefore(it, dummy)
        }
        //Save dummy node to state
        this.add(dummy)
    }

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

            //Fire onAttach listeners
            field.value.checkAttached()
        }
    }
    class NodeMapping(private var node: Node,
                      private val field: Field<out Node>): DomMapping(listOf(field)) {
        override fun update() {
            node.parentNode!!.replaceChild(field.value, node)
            node = field.value
        }
    }
    class ComponentListMapping(private val oldState: MutableList<Node>,
                               private val list: ComponentList<out Component>):
            DomMapping(listOf(list.internalField)) {
        override fun update() {
            //Replace old nodes
            oldState.replaceWith(list.map { it.compiledDom.root })

            //Fire onAttach listeners
            list.forEach {
                it.checkAttached()
            }
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
    class FieldMapping(private val field: Field<String>,
                       private val chunks: List<TextChunk>):
            DomMapping(chunks
                    .filterIsInstance<TextChunk.Field>()
                    .map(TextChunk.Field::field)) {
        override fun update() {
            field.value = chunks.joinToString(separator = "") {
                when (it) {
                    is TextChunk.Text -> it.value
                    is TextChunk.Field -> it.field.value.toString()
                }
            }
        }
    }
    class CustomElementContentMapping(private val oldState: MutableList<Node>,
                                      private val field: ReadOnlyField<CustomElementContent>):
            DomMapping(listOf(field)) {
        override fun update() {
            oldState.replaceWith(field.value.childNodes)
        }
    }
}
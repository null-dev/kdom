package xyz.nulldev.kdom

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.asList
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.ComponentList
import xyz.nulldev.kdom.api.Element
import xyz.nulldev.kdom.api.Field
import kotlin.browser.document
import kotlin.dom.clear

class CompiledDom(val root: HTMLElement,
                  val mappings: List<DomMapping>) {
    companion object {
        const val ELEMENT_TRIGGER = "kref"

        fun fromHtml(html: HTMLElement,
                     fields: List<Field<out Any>>,
                     elements: List<Element<out HTMLElement>>,
                     lists: List<ComponentList<out Component>>): CompiledDom {
            //Include field based component lists in search
            val fieldStringMappings = (fields + lists.map { it.internalField }).associateBy { it.toString() }
            val elementStringMappings = elements.associateBy { it.toString() }

            fun chunkify(text: String): Pair<Boolean, List<TextChunk>> {
                val curChunks = mutableListOf<TextChunk>(TextChunk.Text(text))

                var split = false

                fieldStringMappings.forEach { (k, v) ->
                    do {
                        var done = true
                        curChunks.filterIsInstance<TextChunk.Text>()
                                .forEachIndexed { index, textChunk ->
                                    val ti = textChunk.value.indexOf(k)

                                    if (ti >= 0) {
                                        //Find before and after values
                                        val before = textChunk.value.substring(0 until ti)
                                        val after = textChunk.value.substring(ti + textChunk.value.length)

                                        //Update chunks
                                        curChunks.removeAt(index)
                                        curChunks.add(index, TextChunk.Text(after))
                                        curChunks.add(index, TextChunk.Field(v))
                                        curChunks.add(index, TextChunk.Text(before))

                                        done = false
                                        split = true
                                    }
                                }
                    } while(!done)
                }

                return Pair(split, curChunks)
            }

            val root = html
            val mappings = mutableListOf<DomMapping>()
            fun traverseNode(element: HTMLElement) {
                //Analyze current element for attribute mappings
                element.attributes.asList()
                        .toList() //Clone attributes list as we will be making changes
                        .forEach {
                    if(it.name == ELEMENT_TRIGGER) {
                        //Map elements
                        elementStringMappings[it.value]?.setVal(element.asDynamic())
                            ?: throw IllegalStateException("Invalid kref value found: ${it.value}")
                        //Remove reference attribute
                        element.removeAttributeNode(it)
                    } else {
                        val res = chunkify(it.value)
                        if (res.first)
                            mappings += DomMapping.AttributeMapping(it, res.second)
                    }
                }

                //Analyze current element for text mappings
                val newChildNodes = mutableListOf<Node>()
                var changed = false
                element.childNodes.asList().forEach {
                    if(it is Text) {
                        val res = chunkify(it.textContent ?: "")
                        if(res.first) {
                            changed = true
                            res.second.forEach {
                                if(it is TextChunk.Text)
                                    newChildNodes += document.createTextNode(it.value)
                                else if(it is TextChunk.Field) {
                                    val n = document.createTextNode("")
                                    newChildNodes += n
                                    mappings += if(it.field.value is Component) {
                                        //Handle component mappings
                                        DomMapping.ComponentMapping(n, it.field as Field<out Component>)
                                    } else {
                                        //Handle component lists
                                        val possibleList = lists.find { list -> list.internalField == it.field }
                                        if(possibleList != null)
                                            DomMapping.ComponentListMapping(mutableListOf(n), possibleList)
                                        else
                                            DomMapping.TextMapping(n, it.field)
                                    }
                                }
                            }
                        } else newChildNodes += it
                    } else newChildNodes += it
                }
                if(changed) {
                    element.clear()
                    newChildNodes.forEach { element.appendChild(it) }
                }

                //Analyze all children
                newChildNodes.filterIsInstance<HTMLElement>().forEach {
                    traverseNode(it)
                }
            }

            traverseNode(root)

            //Apply all mappings
            mappings.forEach(DomMapping::update)

            return CompiledDom(root, mappings)
        }
    }
}
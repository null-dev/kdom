package xyz.nulldev.kdom

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.asList
import xyz.nulldev.kdom.api.*
import kotlin.browser.document
import kotlin.dom.clear

class CompiledDom(val root: HTMLElement,
                  val mappings: List<DomMapping>) {
    companion object {
        const val ELEMENT_TRIGGER = "kref"

        fun fromHtml(html: HTMLElement,
                     fields: List<Field<out Any>>,
                     elements: List<Element<out HTMLElement>>,
                     lists: List<ComponentList<out Component>>,
                     component: Component): CompiledDom {
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
                                        val after = textChunk.value.substring(ti + k.length)

                                        //Update chunks
                                        curChunks.removeAt(index)
                                        if(after.isNotEmpty())
                                            curChunks.add(index, TextChunk.Text(after))
                                        curChunks.add(index, TextChunk.Field(v))
                                        if(before.isNotEmpty())
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
                //Analyze all children first
                element.childNodes.asList()
                        .toList() //Clone as we will be making changes
                        .filterIsInstance<HTMLElement>().forEach {
                    traverseNode(it)
                }

                val custom = SpecManager.specFor(element.tagName)
                val customAttributes = mutableMapOf<String, ReadOnlyField<String>>()
                var customElementReference: Element<*>? = null

                //Analyze current element for attribute mappings
                element.attributes.asList()
                        .toList() //Clone attributes list as we will be making changes
                        .forEach {
                    if(it.name == ELEMENT_TRIGGER) {
                        //Map elements
                        val mapping = elementStringMappings[it.value]
                                ?: throw IllegalStateException("Invalid kref value found: ${it.value}")

                        if(custom == null)
                            mapping.setVal(element.asDynamic())
                        else
                            customElementReference = mapping

                        //Remove reference attribute
                        element.removeAttributeNode(it)
                    } else {
                        val res = chunkify(it.value)
                        if(custom != null) {
                            //Custom element attribute mapping
                            val customField: ReadOnlyField<String>

                            if(res.first) {
                                //Dynamic mapping
                                customField = ReadOnlyField(Component.nextId(), "")
                                mappings += DomMapping.FieldMapping(customField, res.second)
                            } else {
                                //Static mapping
                                customField = ReadOnlyField(Component.nextId(), it.value)
                            }

                            customAttributes.put(it.name, customField)
                        } else {
                            //Normal element attribute mapping
                            if (res.first)
                                mappings += DomMapping.AttributeMapping(it, res.second)
                        }
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
                                    mappings += when(it.field.value) {
                                        is Component -> {
                                            //Handle component mappings
                                            DomMapping.ComponentMapping(n, it.field as Field<out Component>)
                                        }
                                        is Node -> {
                                            //Handle node mappings
                                            DomMapping.NodeMapping(n, it.field as Field<out Node>)
                                        }
                                        is CustomElementContent -> {
                                            //Handle custom element content
                                            DomMapping.CustomElementContentMapping(mutableListOf(n),
                                                    it.field as ReadOnlyField<CustomElementContent>)
                                        }
                                        else -> {
                                            //Handle component lists
                                            val possibleList = lists.find { list -> list.internalField == it.field }
                                            if(possibleList != null)
                                                DomMapping.ComponentListMapping(mutableListOf(n), possibleList)
                                            else
                                                DomMapping.TextMapping(n, it.field)
                                        }
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

                //Replace element with spec if custom
                if(custom != null) {
                    val content = ReadOnlyField(Component.nextId(), CustomElementContent(newChildNodes))
                    val root = Element<HTMLElement>(Component.nextId(), null)
                    val request = CustomElementRequest(component,
                            customAttributes,
                            content,
                            element.parentElement as HTMLElement,
                            root)

                    val generated = custom.spec(request)
                    //Import attribute fields
                    customAttributes.values.forEach { generated.internalImportRelay(it) }
                    //Import content
                    generated.internalImportRelay(content)

                    //Update root element
                    root.parent = generated
                    root.setVal(generated.compiledDom.root)

                    //Update element reference
                    customElementReference?.setVal(generated.compiledDom.root.asDynamic())

                    //Replace custom element with placeholder
                    val placeholder = document.createTextNode("")
                    element.replaceWith(placeholder)

                    //Register field + mapping to enable real-element replacement later on
                    val field = ReadOnlyField(Component.nextId(), generated)
                    component.internalImportRelay(field)
                    mappings += DomMapping.ComponentMapping(placeholder, field)
                }
            }

            traverseNode(root)

            //Apply all mappings
            mappings.forEach(DomMapping::update)

            return CompiledDom(root, mappings)
        }
    }
}
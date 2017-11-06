package xyz.nulldev.kdom

import kotlinx.html.dom.create
import org.w3c.dom.*
import xyz.nulldev.kdom.api.*
import xyz.nulldev.kdom.api.Element
import kotlin.browser.document
import kotlin.dom.clear

class CompiledDom(val root: HTMLElement,
                  mappings: List<DomMapping>) {

    private val internalMappings = mappings.toMutableList()
    //Expose mappings as immutable list
    val mappings: List<DomMapping> = internalMappings

    companion object {
        const val REFERENCE_ATTRIBUTE = "kref"
        const val STYLE_ATTRIBUTE = "kstyle"

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
                        curChunks.mapIndexed { index, chunk -> Pair(index, chunk) }
                                .filter { it.second is TextChunk.Text }
                                .forEach { (index, textChunk) ->
                                    //Manually confirm that textChunk is TextChunk.Text (as we can't use filterIsInstance)
                                    textChunk as TextChunk.Text

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

                val classesToAppend = mutableListOf<String>()

                //Analyze current element for attribute mappings
                element.attributes.asList()
                        .toList() //Clone attributes list as we will be making changes
                        .forEach {
                            if(it.name == REFERENCE_ATTRIBUTE) {
                                //Map elements
                                val mapping = elementStringMappings[it.value]
                                        ?: throw IllegalStateException("Invalid kref value found: ${it.value}")

                                if(custom == null)
                                    mapping.setVal(element.asDynamic())
                                else
                                    customElementReference = mapping

                                //Remove reference attribute
                                element.removeAttributeNode(it)
                            } else if(it.name == STYLE_ATTRIBUTE) {
                                val clazz = StyleManager.nextIdClass()
                                val newVal = it.value.replaceFirst(ElementStyle.PLACEHOLDER_STYLE_CLASS, clazz)
                                val res = chunkify(newVal)

                                val nodes = res.second.map {
                                    when (it) {
                                        is TextChunk.Text -> document.createTextNode(it.value)
                                        is TextChunk.Field -> {
                                            val node = document.createTextNode("")
                                            mappings += DomMapping.TextMapping(node, it.field)
                                            node
                                        }
                                    }
                                }

                                // Inject and add style to element
                                val createdStyle = StyleManager.createStyle(clazz, nodes)
                                classesToAppend += createdStyle.first

                                //Associate style with component
                                component.associatedStyleElements += createdStyle

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

                                    customAttributes.put(it.name.toLowerCase(), customField)
                                } else {
                                    //Normal element attribute mapping
                                    if (res.first)
                                        mappings += DomMapping.AttributeMapping(it, res.second)
                                }
                            }
                        }

                //Append classes
                if(classesToAppend.isNotEmpty()) {
                    val newClassString = classesToAppend.joinToString(" ")
                    //Find and replace mapping
                    val elementAttribute = element.getAttributeNode("class") ?: document.createAttribute("class")
                    val classAttrMapping = mappings.filterIsInstance<DomMapping.AttributeMapping>().find {
                        it.attr == elementAttribute
                    }
                    if (classAttrMapping != null) {
                        // Remove old mapping
                        mappings.remove(classAttrMapping)
                        mappings += DomMapping.AttributeMapping(elementAttribute,
                                classAttrMapping.chunks + TextChunk.Text(" " + newClassString))
                    }
                    //Set original attribute
                    elementAttribute.value += " " + newClassString
                    //Change custom element mappings
                    if (custom != null) {
                        //Find and transform original field or create new mapping
                        val new = customAttributes["class"]?.transform {
                            it + " " + newClassString
                        } ?: ReadOnlyField(Component.nextId(), newClassString)
                        //Apply change
                        customAttributes["class"] = new
                    }
                    //Ensure attribute is attached to element
                    element.setAttributeNode(elementAttribute)
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

                    //Update root element parent
                    root.parent = generated

                    //Silently compile DOM
                    generated.silentlyCompile()
                    root.setVal(generated.compiledDom.root)

                    //Merge attributes
                    val toUpdate = mutableListOf<DomMapping>()
                    fun appendToAttr(attr: String, chunks: List<TextChunk>) {
                        //Find old mappings in generated component
                        val mapping = generated.compiledDom.mappings.filterIsInstance<DomMapping.AttributeMapping>().find {
                            it.attr.name.equals(attr, true)
                        }?.apply {
                            //Remove old mapping
                            generated.compiledDom.internalMappings.remove(this)
                        }

                        //Assemble new chunks (use attribute values in HTML if not in mappings)
                        val newChunks = (mapping?.chunks ?: listOf(TextChunk.Text(root.value.getAttribute(attr) ?: ""))) + chunks

                        //Create new attribute node
                        val attrNode = document.createAttribute(attr)
                        root.value.setAttributeNode(attrNode)

                        //Add new mappings
                        val newMapping = DomMapping.AttributeMapping(attrNode, newChunks)
                        generated.compiledDom.internalMappings += newMapping
                        toUpdate += newMapping
                    }
                    customAttributes.forEach { (name, value) ->
                        when(name.toLowerCase()) {
                            "id" -> {
                                //Check if ID already set!
                                if(root.value.hasAttribute("id")) {
                                    console.warn("Could not merge id attribute! The custom element's tag contains an ID attribute but the custom element's root element also contains an ID attribute!")
                                } else {
                                    //Add new ID mapping
                                    val idAttr = document.createAttribute("id")
                                    val newMapping = DomMapping.AttributeMapping(idAttr,
                                            listOf(TextChunk.Field(generated.internalImportRelay(value))))
                                    generated.compiledDom.internalMappings += newMapping
                                    toUpdate += newMapping
                                }
                            }
                            "class" -> {
                                appendToAttr("class", listOf(
                                        TextChunk.Text(" "),
                                        TextChunk.Field(generated.internalImportRelay(value))
                                ))
                            }
                            "style" -> {
                                appendToAttr("style", listOf(
                                        TextChunk.Text(";"),
                                        TextChunk.Field(generated.internalImportRelay(value))
                                ))
                            }
                        }
                    }
                    toUpdate.forEach { it.update() }

                    //Trigger compile events
                    generated.triggerCompileEvents()

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
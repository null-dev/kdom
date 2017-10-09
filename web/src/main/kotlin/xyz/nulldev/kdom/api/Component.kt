package xyz.nulldev.kdom.api

import kotlinx.html.Tag
import org.w3c.dom.HTMLElement
import xyz.nulldev.kdom.CompiledDom
import xyz.nulldev.kdom.util.HUGE_STRING
import kotlin.browser.document

abstract class Component {
    @JsName("domGenerator")
    abstract fun dom(): HTMLElement

    @JsName("attachListener")
    open fun onAttach() {}

    private val registeredFields = mutableMapOf<Long, Field<out Any>>()
    private val registeredElements = mutableMapOf<Long, Element<out HTMLElement>>()
    private val registeredLists = mutableMapOf<Long, ComponentList<out Component>>()

    var attached = false
        private set(value) {
            if(!value && field) {
                throw IllegalStateException("Components cannot be unattached!")
            }

            field = value
            if(value)
                onAttach()
        }

    //Creation elements
    protected open fun <T : Any> field(initialValue: T): Field<T> {
        val id = lastId++
        val field = Field(id, initialValue, { field ->
            //Update mappings when variable changes
            compiledDom.mappings.forEach {
                if(it.fields.contains(field))
                    it.update()
            }
        })
        registeredFields.put(id, field)
        return field
    }

    protected open fun <T : HTMLElement> element(): Element<T> {
        val id = lastId++
        val element = Element<T>(id, this)
        registeredElements.put(id, element)
        return element
    }

    //Alias to element (but without types)
    protected open fun htmlElement(): Element<HTMLElement> {
        return element()
    }

    protected open fun <T : Component> componentList(vararg initialValues: T): ComponentList<T> {
        val id = lastId++
        val clist = ComponentList(id, { list ->
            //Update mappings when list changes
            compiledDom.mappings.forEach {
                if(it.fields.contains(list.internalField))
                    it.update()
            }
        }, mutableListOf(*initialValues))
        registeredLists.put(id, clist)
        return clist
    }

    //Importing
    protected open fun <T : Any> import(field: Field<T>): Field<T> {
        if(!registeredFields.containsKey(field.id)) {
            registeredFields.put(field.id, field)
            field.updateListeners.add({ f ->
                //Update mappings when variable changes
                compiledDom.mappings.forEach {
                    if (it.fields.contains(f))
                        it.update()
                }
            })
        }
        return field
    }
    protected open fun <T : HTMLElement> import(element: Element<T>): Element<T> {
        if(!registeredElements.containsKey(element.id))
            registeredElements.put(element.id, element)
        return element
    }
    protected open fun <T : Component> import(components: ComponentList<T>): ComponentList<T> {
        if(!registeredLists.containsKey(components.id)) {
            registeredLists.put(components.id, components)
            components.updateListeners.add({ list ->
                //Update mappings when list changes
                compiledDom.mappings.forEach {
                    if (it.fields.contains(list.internalField))
                        it.update()
                }
            })
        }
        return components
    }

    // DOM compiler
    private var realCompiledDom: CompiledDom? = null
    val compiledDom: CompiledDom
        get() {
            if(realCompiledDom == null) {
                realCompiledDom = CompiledDom.fromHtml(dom(),
                        registeredFields.values.toList(),
                        registeredElements.values.toList(),
                        registeredLists.values.toList())
                attached = true
            }
            return realCompiledDom!!
        }

    //Extension functions
    fun String.toDom(): HTMLElement {
        val trimmed = this.trim()
        if(trimmed.startsWith("$HUGE_STRING-kdom")
                && trimmed.endsWith("kdom-$HUGE_STRING")) {
            throw IllegalArgumentException("Components cannot consist of a single field/element/list, please add a wrapper element around it instead!")
        }
        val t = document.createElement("template")
        t.innerHTML = this
        val out = t.asDynamic().content.cloneNode(true).children
        if(out.length > 1)
            throw IllegalArgumentException("Components that contain multiple elements must be wrapped in a parent element!")
        return out[0]
    }

    var Tag.kref: Element<*>
        get() = throw UnsupportedOperationException("This attribute can only be written to!")
        set(value) {
            attributes.put(CompiledDom.ELEMENT_TRIGGER, value.toString())
        }

    companion object {
        private var lastId = 0L

        fun from(domBuilder: ComponentBuilder.() -> HTMLElement): Component {
            return object: ComponentBuilder() {
                override fun dom() = domBuilder(this)
            }
        }
    }
}
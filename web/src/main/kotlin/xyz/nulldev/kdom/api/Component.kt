package xyz.nulldev.kdom.api

import azadev.kotlin.css.Stylesheet
import kotlinx.html.Tag
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import xyz.nulldev.kdom.CompiledDom
import xyz.nulldev.kdom.api.util.async
import xyz.nulldev.kdom.util.HUGE_STRING
import kotlin.browser.document

abstract class Component {
    /**
     * The root HTML element of this page
     */
    @JsName("domGenerator")
    abstract fun dom(): HTMLElement

    /**
     * Executed when the component is compiled
     */
    @JsName("compileListener")
    open suspend fun onCompile() {}

    /**
     * Executed when the component is attached to the document
     */
    @JsName("attachListener")
    open suspend fun onAttach() {}

    /**
     * Executed when the component is detached from the document
     */
    @JsName("detachListener")
    open suspend fun onDetach() {}

    private val registeredFields = mutableMapOf<Long, Field<out Any>>()
    private val registeredElements = mutableMapOf<Long, Element<out HTMLElement>>()
    private val registeredLists = mutableMapOf<Long, ComponentList<out Component>>()
    internal val associatedStyleElements = mutableMapOf<String, org.w3c.dom.Element>()

    /**
     * Whether or not this component has been compiled
     */
    var compiled = false
        private set(value) {
            if(!value && field) {
                throw IllegalStateException("Components cannot be uncompiled!")
            }

            field = value
            if(value)
                async { onCompile() }
        }

    /**
     * Whether or not this component is attached to the document
     */
    val attached: Boolean
        get() {
            checkAttached()
            return internalAttached
        }

    /**
     * Whether or not this component is attached to the document
     *
     * To be used in the future for caching if possible
     */
    private var internalAttached = false

    /**
     * Recalculate `internalAttached`
     */
    fun checkAttached() {
        fun findUltimateAncestor(node: Node): Node? {
            // Walk up the DOM tree until we are at the top (parentNode
            // will return null at that point).
            // NOTE: this will return the same node that was passed in
            // if it has no ancestors.
            var ancestor: Node? = node
            while(ancestor?.parentNode != null) {
                ancestor = ancestor.parentNode
            }
            return ancestor
        }
        val newVal = findUltimateAncestor(compiledDom.root).asDynamic()?.body != null

        if(!internalAttached && newVal) {
            attachStyles()
            async { onAttach() }
        } else if(internalAttached && !newVal) {
            detachStyles()
            async { onDetach() }
        }

        internalAttached = newVal

        // Fire attach listeners for all children
        registeredFields.values.forEach {
            (it.value as? Component)?.checkAttached()
        }
        registeredLists.values.forEach {
            it.forEach(Component::checkAttached)
        }
    }

    private fun attachStyles() {
        associatedStyleElements.forEach {
            document.head?.appendChild(it.value)
                    ?: throw IllegalStateException("Document head not found!")
        }
    }

    private fun detachStyles() {
        associatedStyleElements.forEach {
            document.head?.removeChild(it.value)
                    ?: throw IllegalStateException("Document head not found!")
        }
    }

    //Creation elements
    /**
     * Create a field in this component
     *
     * @param initialValue The initial value of the field
     * @return The created field
     */
    protected open fun <T : Any> field(initialValue: T): Field<T> {
        val field = Field(nextId(), initialValue)
        import(field)
        return field
    }

    /**
     * Create an element reference in this component
     *
     * @return The created element reference
     */
    protected open fun <T : HTMLElement> element(): Element<T> {
        val id = nextId()
        val element = Element<T>(id, this)
        registeredElements.put(id, element)
        return element
    }

    /**
     * Alias to `element()` without generics
     */
    protected open fun htmlElement(): Element<HTMLElement> {
        return element()
    }

    /**
     * Create a component list in this component
     *
     * @param initialValues The initial content of the component list
     * @return The created component list
     */
    protected open fun <T : Component> componentList(vararg initialValues: T): ComponentList<T> {
        val id = nextId()
        val clist = ComponentList(id, mutableListOf(*initialValues))
        import(clist)
        return clist
    }

    /**
     * Create an element style
     */
    protected open fun style(gen: Stylesheet.() -> Unit) = ElementStyle(gen)
    protected open fun style(content: String) = ElementStyle(content)

    //Importing

    /**
     * Internal alias to `import()` with more lenient visibility modifiers
     */
    internal fun <T : Any> internalImportRelay(field: Field<T>) = import(field)

    /**
     * Import a field into this component
     * Allows the use of an external field in string substitutions in this component
     *
     * @param field The field to import
     * @return The imported field (same as original field instance)
     */
    protected open fun <T : Any> import(field: Field<T>): Field<T> {
        if(!registeredFields.containsKey(field.id)) {
            registeredFields.put(field.id, field)

            if(!field.parentComponents.contains(this))
                field.parentComponents.add(this)
        }
        return field
    }

    /**
     * Import a component list into this component
     * Allows the use of an external component list in string substitutions in this component
     *
     * @param components The component list to import
     * @return The imported field (same as original field instance)
     */
    protected open fun <T : Component> import(components: ComponentList<T>): ComponentList<T> {
        if(!registeredLists.containsKey(components.id)) {
            registeredLists.put(components.id, components)

            if(!components.parentComponents.contains(this))
                components.parentComponents.add(this)
        }
        return components
    }

    // DOM compiler

    // Internal compiled copy of dom
    private var realCompiledDom: CompiledDom? = null

    /**
     * Compiled DOM for access to the DOM's HTML content and mappings
     *
     * Evaluated on demand
     */
    val compiledDom: CompiledDom
        get() {
            if(realCompiledDom == null) {
                silentlyCompile()
                triggerCompileEvents()
            }
            return realCompiledDom!!
        }

    internal fun silentlyCompile() {
        if(realCompiledDom == null) {
            realCompiledDom = CompiledDom.fromHtml(dom(),
                    registeredFields.values.toList(),
                    registeredElements.values.toList(),
                    registeredLists.values.toList(),
                    this)

            //Register compiled DOM
            realCompiledDom!!.root.asDynamic()[COMPONENT_KEY] = this
        }
    }

    internal fun triggerCompileEvents() {
        if(!compiled)
            compiled = true
    }

    //Extension functions
    /**
     * Parse an HTML string in an HTML component
     *
     * Does NOT allow multiple root components
     */
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
        if(out[0].tagName.includes("-"))
            throw IllegalArgumentException("Components that contain a single custom element must be wrapped in a parent element!")
        return out[0]
    }

    /**
     * KotlinX HTML alias to the kref attribute
     */
    var Tag.kref: Element<*>
        get() = throw UnsupportedOperationException("This attribute can only be written to!")
        set(value) {
            attributes.put(CompiledDom.REFERENCE_ATTRIBUTE, value.toString())
        }

    companion object {
        internal val COMPONENT_KEY = "${HUGE_STRING}_COMPONENT"
        private var lastId = 0L

        // Next field/element/list ID
        internal fun nextId() = lastId++

        /**
         * Convenience method used to build Components dynamically
         */
        fun from(domBuilder: ComponentBuilder.() -> HTMLElement): Component {
            return object: ComponentBuilder() {
                override fun dom() = domBuilder(this)
            }
        }
    }
}
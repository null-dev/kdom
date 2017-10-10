package xyz.nulldev.kdom.api

import org.w3c.dom.HTMLElement

abstract class ComponentBuilder: Component() {
    var onCompile = {}
    var onAttach = {}

    override fun onCompile() {
        //Hack to reference onCompile function reference
        onCompile.apply { this() }
    }

    override fun onAttach() {
        //Hack to reference onAttach function reference
        onAttach.apply { this() }
    }

    //Expose protected methods
    public override fun <T : Any> field(initialValue: T) = super.field(initialValue)
    public override fun <T : HTMLElement> element() = super.element<T>()
    public override fun htmlElement() = super.htmlElement()
    public override fun <T : Component> componentList(vararg initialValues: T) = super.componentList(*initialValues)
    public override fun <T : Any> import(field: Field<T>)=  super.import(field)
    public override fun <T : HTMLElement> import(element: Element<T>) = super.import(element)
    public override fun <T : Component> import(components: ComponentList<T>) = super.import(components)
}
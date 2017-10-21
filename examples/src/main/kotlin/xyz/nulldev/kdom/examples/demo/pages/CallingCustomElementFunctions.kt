package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.CustomElementRequest
import xyz.nulldev.kdom.api.CustomElementSpec
import xyz.nulldev.kdom.api.SpecManager
import xyz.nulldev.kdom.api.util.component
import xyz.nulldev.kdom.examples.demo.DemoPage

class CallingCustomElementFunctions : DemoPage("Calling functions on custom elements",
        "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/CallingCustomElementFunctions.kt") {
    init {
        SpecManager.registerSpec(ExampleCustomElement.spec)
    }

    private val example = htmlElement()

    override suspend fun onAttach() {
        example().onclick = {
            example.component<ExampleCustomElement>().performAction()
        }
    }

    override fun dom() = """
        <div>
            <example-custom-element kref="$example"></example-custom-element>
        </div>
        """.toDom()
}

class ExampleCustomElement(val req: CustomElementRequest) : Component() {
    private val content = field("Click me!")

    override fun dom() = """
            <h1 class="mdc-typography--display4">$content</h1>
        """.toDom()

    fun performAction() {
        content("You clicked me!")
    }

    companion object {
        val spec = CustomElementSpec("example-custom-element", {
            ExampleCustomElement(it)
        })
    }
}

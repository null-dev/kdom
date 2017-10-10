package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.examples.demo.DemoPage

class HelloWorld:
        DemoPage("Hello world!", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/HelloWorld.kt") {
    private val title = field("World")
    private val button = htmlElement()

    //language=html
    override fun dom() = """
        <div>
            <h1 class="mdc-typography--display4">Hello $title!</h1>
            <button class="mdc-button mdc-button--raised" kref="$button">Click me!</button>
        </div>
        """.toDom()

    override fun onCompile() {
        button().onclick = {
            title("Universe")
        }
    }
}
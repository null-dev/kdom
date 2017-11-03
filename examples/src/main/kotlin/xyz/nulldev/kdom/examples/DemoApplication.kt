package xyz.nulldev.kdom.examples

import xyz.nulldev.kdom.api.Application
import xyz.nulldev.kdom.examples.demo.DemoRoot
import xyz.nulldev.kdom.examples.demo.pages.*
import kotlin.browser.document
import kotlin.browser.window

object DemoApplication : Application() {
    val demoRoot = DemoRoot(mapOf(
            "/hello-world" to { HelloWorld() },
            "/dynamic-attributes" to { DynamicAttributesDemo() },
            "/lists" to { ListDemo() },
            "/custom-elements" to { CustomElementDemo() },
            "/calling-custom-element-functions" to { CallingCustomElementFunctions() },
            "/mdcshim-demo" to { MDCShimDemo() },
            "/stack-based-routing/" to { StackBasedRoutingDemo() }
    ))

    override val router = demoRoot
    override val rootComponent = demoRoot
}

fun main(args: Array<String>) {
    window.onload = {
        DemoApplication.attach(document)
    }
}
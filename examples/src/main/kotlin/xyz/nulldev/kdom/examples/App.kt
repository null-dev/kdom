package xyz.nulldev.kdom.examples

import xyz.nulldev.kdom.examples.demo.DemoRoot
import xyz.nulldev.kdom.examples.demo.pages.*
import kotlin.browser.document
import kotlin.browser.window

fun main(args: Array<String>) {
    window.onload = {
        val page = DemoRoot(listOf(
                {HelloWorld()},
                {DynamicAttributesDemo()},
                {ListDemo()},
                {CustomElementDemo()},
                {CallingCustomElementFunctions()}
        ))
        document.body!!.appendChild(page.compiledDom.root)
        page.checkAttached()
    }
}


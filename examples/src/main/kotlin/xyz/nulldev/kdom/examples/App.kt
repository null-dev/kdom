package xyz.nulldev.kdom.examples

import kotlin.browser.document
import kotlin.browser.window

fun main(args: Array<String>) {
    window.onload = {
        val page = TestPage()
        document.body!!.appendChild(page.compiledDom.root)
        null
    }
}


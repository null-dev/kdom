package xyz.nulldev.kdom.examples

import xyz.nulldev.kdom.api.Component

class Header : Component() {
    val htext = field("World!")
    val btn = htmlElement()

    override fun dom() = """
        <div>
            <h1>Hello $htext</h1>
            <button kref="$btn">Click me!</button>
        </div>
        """.toDom()

    override fun onAttach() {
        btn().onclick = {
            htext("Universe!")
        }
    }
}
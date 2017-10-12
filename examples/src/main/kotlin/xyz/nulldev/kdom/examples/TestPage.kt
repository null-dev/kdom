package xyz.nulldev.kdom.examples

import org.w3c.dom.HTMLButtonElement
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.examples.notes.NoteApp

class TestPage: Component() {
    val title = field("Test!")
    val button = element<HTMLButtonElement>()
    val dslTest = field(KotlinDSLElement())
    val noteApp = field(NoteApp())

    override suspend fun onCompile() {
        button.value.onclick = {
            title.value = "You clicked the button!"
            null
        }
    }

    override fun dom() = """
        <div>
            <h1>$title</h1>
            <button kref="$button">Click me!</button>
            <br>
            $dslTest
            <br>
            $noteApp
        </div>
        """.toDom()
}
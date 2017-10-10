package xyz.nulldev.kdom.examples.notes

import org.w3c.dom.HTMLButtonElement
import xyz.nulldev.kdom.api.Component

class NoteApp: Component() {
    val notes = componentList<Note>()
    private val addNoteswBtn = element<HTMLButtonElement>()

    override fun onCompile() {
        addNoteswBtn.value.onclick = {
            notes.add(Note(this))
        }
    }

    override fun dom() = """
        <div>
            <h1>Simple Notes App</h1>
            <div>
                $notes
                <button kref="$addNoteswBtn">Add note</button>
            </div>
        </div>
        """.toDom()

}
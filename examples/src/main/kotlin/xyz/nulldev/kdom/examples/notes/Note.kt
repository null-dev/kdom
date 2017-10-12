package xyz.nulldev.kdom.examples.notes

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLTextAreaElement
import xyz.nulldev.kdom.api.Component
import kotlin.browser.window

class Note(val parent: NoteApp): Component() {
    val view = field<Component>(CompactView(this))

    val title = field("Title (click to expand and minimize)!")
    val body = field("Body (click to edit, click elsewhere to save)")

    override fun dom() = """
            <div style="border: solid 1px grey; padding: 8px">$view</div>
        """.toDom()

    private class ExpandedView(val parent: Note): Component() {
        private val titleRef = element<HTMLElement>()
        private val view = field(roView())
        private val deleteBtn = element<HTMLButtonElement>()

        override fun dom() = """
            <div>
                <h3 style="margin-top:0" kref="$titleRef">${import(parent.title)}</h3>
                <div style="border: solid 1px grey">$view</div>
                <button kref="$deleteBtn">Delete</button>
            </div>
            """.toDom()

        override suspend fun onCompile() {
            titleRef().onclick = {
                parent.view(CompactView(parent))
            }
            deleteBtn().onclick = {
                parent.parent.notes.remove(parent)
            }
        }

        fun roView(): Component = Component.from {
            val pRef = element<HTMLParagraphElement>()
            onAttach = {
                pRef().onclick = { view(editView()) }
            }
            """<p kref="$pRef">${import(parent.body)}</p>""".toDom()
        }

        fun editView(): Component = Component.from {
            val editRef = element<HTMLTextAreaElement>()
            onAttach = {
                editRef().onblur = {
                    parent.body(editRef().value)
                    view(roView())
                }
                //Focus textarea
                window.setTimeout({
                    editRef().focus()
                }, 0)
            }
            """<textarea style="width:100%;" rows="5" kref="$editRef">${import(parent.body)}</textarea>""".toDom()
        }
    }
    private class CompactView(val parent: Note): Component() {
        private val ref = element<HTMLElement>()

        override fun dom() = """
            <div kref="$ref"><b>${import(parent.title)}</b> <span>${import(parent.body)}</span></div>
            """.toDom()

        override suspend fun onCompile() {
            ref().onclick = {
                parent.view(ExpandedView(parent))
            }
        }
    }
}

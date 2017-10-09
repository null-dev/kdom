package xyz.nulldev.kdom.examples

import org.w3c.dom.HTMLButtonElement
import xyz.nulldev.kdom.api.Component

class ListExample: Component() {
    val listItems = componentList<ListItem>()
    val addButton = element<HTMLButtonElement>()

    override fun onAttach() {
        var lastIndex = 0
        addButton().onclick = {
            listItems.add(ListItem(++lastIndex, this))
        }
    }

    override fun dom() = """
        <div>
            $listItems
            <button kref="$addButton">Add item</button>
        </div>
        """.toDom()
}
class ListItem(val index: Int, val parent: ListExample): Component() {
    private val removeButton = element<HTMLButtonElement>()

    override fun onAttach() {
        removeButton().onclick = {
            parent.listItems.remove(this)
        }
    }

    override fun dom() = """
        <div>$index: I am a list item! <button kref="$removeButton">Remove</button></div>
        """.toDom()
}
package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.examples.demo.DemoPage

class ListDemo:
        DemoPage("List demo", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/ListDemo.kt") {
    val listItems = componentList<ListItem>()
    val addButton = htmlElement()

    override fun onCompile() {
        var lastIndex = 0
        addButton().onclick = {
            listItems.add(ListItem(++lastIndex, this))
        }
    }

    //language=html
    override fun dom() = """
        <nav class="mdc-list">
            $listItems
            <button kref="$addButton" style="width:100%" class="mdc-button mdc-button--raised">
                Add item
            </button>
        </nav>
        """.toDom()
}

class ListItem(val index: Int, val parent: ListDemo): Component() {
    private val removeButton = htmlElement()

    override fun onCompile() {
        removeButton().onclick = {
            parent.listItems.remove(this)
        }
    }

    //language=html
    override fun dom() = """
        <span class="mdc-list-item" style="cursor:pointer">
            <i class="material-icons mdc-list-item__start-detail" aria-hidden="true">
                bluetooth
            </i>
            $index: I am a list item!
            <span kref="$removeButton" style="cursor:pointer" class="mdc-list-item__end-detail material-icons"
                aria-label="Remove" title="Remove">
                delete
            </span>
        </span>
        """.toDom()
}

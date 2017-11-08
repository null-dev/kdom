package xyz.nulldev.kdom.examples.demo.pages

import azadev.kotlin.css.backgroundColor
import azadev.kotlin.css.colors.rgb
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.style
import xyz.nulldev.kdom.examples.demo.DemoPage

class StyledListDemo:
        DemoPage("Styled list demo", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/StyledListDemo.kt") {
    val listItems = componentList<StyledListItem>()
    val addButton = htmlElement()

    override suspend fun onCompile() {
        var lastIndex = 0
        addButton().onclick = {
            listItems.add(StyledListItem(++lastIndex, this))
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

class StyledListItem(val index: Int, val parent: StyledListDemo): Component() {
    private val removeButton = htmlElement()

    private val itemStyle = style {
        hover {
            backgroundColor = rgb(220, 220, 220)
        }
    }

    override suspend fun onCompile() {
        removeButton().onclick = {
            parent.listItems.remove(this)
        }
    }

    //language=html
    override fun dom() = """
        <span class="mdc-list-item" style="cursor:pointer" kstyle="$itemStyle">
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
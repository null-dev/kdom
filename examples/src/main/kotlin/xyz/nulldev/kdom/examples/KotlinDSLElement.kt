package xyz.nulldev.kdom.examples

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import xyz.nulldev.kdom.api.Component
import kotlin.browser.document
import kotlin.browser.window

class KotlinDSLElement: Component() {
    private val selected = field("???")
    private val selectionColor = field("white")
    private val selectionStatus = element<HTMLDivElement>()

    override fun onAttach() {
        selectionStatus.value.onclick = {
            if(selectionColor.value == "green")
                selectionColor.value = "white"
            else
                selectionColor.value = "green"
            null
        }
    }

    override fun dom() = document.create.div {
        form {
            listOf(
                    "Oranges",
                    "Bananas",
                    "Cheese",
                    "Lemons",
                    "Beer"
            ).forEach { item ->
                input(InputType.radio) {
                    name = "food"
                    onClickFunction = { //You can use kref or just use the kotlin.htmlx listeners directly
                        window.alert("You selected $item!")
                        selected.value = item
                    }
                }
                span { +item }
                br
            }
        }
        div {
            style = "background-color: $selectionColor"
            +"The currently selected item is: $selected!"
            kref = selectionStatus //You can use kref or just use the kotlin.htmlx listeners directly
        }
    }
}
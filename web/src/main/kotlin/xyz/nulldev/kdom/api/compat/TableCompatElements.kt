package xyz.nulldev.kdom.api.compat

import org.w3c.dom.HTMLElement
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.CustomElementRequest
import xyz.nulldev.kdom.api.CustomElementSpecFactory
import xyz.nulldev.kdom.api.KdomLogger
import kotlin.browser.document
import kotlin.dom.clear

object KTableSpec : CustomElementSpecFactory {
    override val tag = "k-table"

    override fun generate(request: CustomElementRequest) = Component.from {
        val root = document.createElement("table")

        fun updateContent() {
            root.clear()

            request.content().childNodes.forEach {
                root.appendChild(it)
            }
        }

        request.content.addUpdateListener {
            updateContent()
        }

        updateContent()

        root as HTMLElement
    }
}

object KTrSpec : CustomElementSpecFactory {
    override val tag = "k-tr"

    override fun generate(request: CustomElementRequest) = Component.from {
        val root = document.createElement("tr")

        fun updateContent() {
            root.clear()

            request.content().childNodes.forEach {
                root.appendChild(it)
            }
        }

        request.content.addUpdateListener {
            updateContent()
        }

        updateContent()

        root as HTMLElement
    }
}

object KTdSpec : CustomElementSpecFactory {
    override val tag = "k-td"

    override fun generate(request: CustomElementRequest) = Component.from {
        //language=html
        "<td>${request.content}</td>".toDom()
    }
}

object KThSpec : CustomElementSpecFactory {
    override val tag = "k-th"

    override fun generate(request: CustomElementRequest) = Component.from {
        //language=html
        "<th>${request.content}</th>".toDom()
    }
}

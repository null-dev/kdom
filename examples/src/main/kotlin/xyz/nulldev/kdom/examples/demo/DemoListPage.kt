package xyz.nulldev.kdom.examples.demo

import xyz.nulldev.kdom.api.Component

class DemoListPage(demoPages: List<() -> DemoPage>,
                   private val parent: DemoRoot): DemoPage("Demo list") {
    private val listItems = componentList(*demoPages.map {
        demoListItem(it)
    }.toTypedArray())

    //language=html
    override fun dom() = """
            <nav class="mdc-list">
                $listItems
            </nav>
        """.toDom()

    private fun demoListItem(page: () -> DemoPage) = Component.from {
        val item = htmlElement()

        var generated = page()
        onAttach = {
            item().onclick = {
                parent.setCurrentContent(page)
                generated = page()
                null
            }
        }

        //language=html
        """
            <span class="mdc-list-item" style="cursor:pointer" kref="$item">
                <i class="material-icons mdc-list-item__start-detail" aria-hidden="true">
                    help
                </i>
                ${generated.name}
            </span>
            """.toDom()
    }
}
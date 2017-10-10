package xyz.nulldev.kdom.examples.demo

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.EmptyComponent

class DemoRoot(pages: List<() -> DemoPage>): Component() {
    private val demoListPage = DemoListPage(pages, this)

    private val currentContent = field<DemoPage>(demoListPage)
    private val backBtnField = field<Component>(EmptyComponent)
    private val title = field(MAIN_TITLE)
    private val resetIconVisibility = field("none")

    private val resetIcon = htmlElement()

    private lateinit var curPageGen: () -> DemoPage

    fun setCurrentContent(newContent: () -> DemoPage) {
        curPageGen = newContent
        backBtnField(backBtn)
        currentContent(newContent())
        title(currentContent().name)
        resetIconVisibility("initial")
    }

    override fun onCompile() {
        resetIcon().onclick = {
            currentContent(curPageGen())
        }
    }

    //language=html
    override fun dom() = """
        <div>
            <header class="mdc-toolbar mdc-toolbar--fixed">
                <div class="mdc-toolbar__row">
                    <section class="mdc-toolbar__section mdc-toolbar__section--align-start">
                        $backBtnField
                        <span class="mdc-toolbar__title catalog-title">$title</span>
                    </section>
                    <section class="mdc-toolbar__section mdc-toolbar__section--align-end" role="toolbar">
                        <span kref="$resetIcon"
                            class="material-icons mdc-toolbar__icon"
                            alt="Reset demo"
                            style="display:$resetIconVisibility">refresh</span>
                    </section>
                </div>
            </header>
            <main class="mdc-toolbar-fixed-adjust">$currentContent</main>
        </div>
        """.toDom()

    val backBtn = Component.from {
        val btn = htmlElement()
        onAttach = {
            btn().onclick = {
                backBtnField(EmptyComponent)
                currentContent(demoListPage)
                title(MAIN_TITLE)
                resetIconVisibility("none")
            }
        }
        //language=html
        """
            <span kref="$btn" class="mdc-toolbar__icon--menu"><i class="material-icons">&#xE5C4;</i></span>
            """.toDom()
    }

    companion object {
        const val MAIN_TITLE = "kdom Demos"
    }
}

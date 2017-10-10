package xyz.nulldev.kdom.examples.demo

import org.w3c.dom.HTMLAnchorElement
import org.w3c.xhr.XMLHttpRequest
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.EmptyComponent
import kotlin.dom.clear

class DemoRoot(pages: List<() -> DemoPage>): Component() {
    private val demoListPage = DemoListPage(pages, this)

    private val currentContent = field<DemoPage>(demoListPage)
    private val backBtnField = field<Component>(EmptyComponent)
    private val title = field(MAIN_TITLE)
    private val resetIconVisibility = field("none")

    private val resetIcon = htmlElement()
    private val codeIcon = htmlElement()
    private val sourceObj = htmlElement()
    private val dialog = htmlElement()
    private val githubLink = element<HTMLAnchorElement>()

    private lateinit var curPageGen: () -> DemoPage

    fun setCurrentContent(newContent: () -> DemoPage) {
        curPageGen = newContent
        backBtnField(backBtn)
        currentContent(newContent())
        title(currentContent().name)
        resetIconVisibility("initial")
        showSources(currentContent().source)
    }

    private fun showSources(sourcePath: String) {
        val xhr = XMLHttpRequest()
        xhr.onreadystatechange = {
            if (xhr.readyState.toInt() == 4 && xhr.status.toInt() == 200) {
                sourceObj().clear()
                sourceObj().textContent = xhr.responseText
                js("hljs").highlightBlock(sourceObj())
                githubLink().href = "https://github.com/null-dev/kdom/blob/master/$sourcePath"
            }
        }
        xhr.open("get", "https://raw.githubusercontent.com/null-dev/kdom/master/$sourcePath", true)
        xhr.send()
    }

    override fun onCompile() {
        resetIcon().onclick = {
            currentContent(curPageGen())
        }
        showSources(demoListPage.source)
    }

    override fun onAttach() {
        val dialogObj = js("(function(d) {return new mdc.dialog.MDCDialog(d);})")(dialog())
        codeIcon().onclick = {
            dialogObj.show()
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
                        <span kref="$codeIcon"
                            class="material-icons mdc-toolbar__icon"
                            alt="Show source code">code</span>
                        <span kref="$resetIcon"
                            class="material-icons mdc-toolbar__icon"
                            alt="Reset demo"
                            style="display:$resetIconVisibility">refresh</span>
                    </section>
                </div>
            </header>
            <main class="mdc-toolbar-fixed-adjust">$currentContent</main>
            <aside class="mdc-dialog"
                   role="alertdialog"
                   aria-hidden="true"
                   aria-labelledby="mdc-dialog-default-label"
                   aria-describedby="mdc-dialog-default-description"
                   kref="$dialog">
                <div class="mdc-dialog__surface">
                    <header class="mdc-dialog__header">
                        <h2 id="mdc-dialog-default-label" class="mdc-dialog__header__title">
                            Source code for: $title
                        </h2>
                    </header>
                    <pre kref="$sourceObj"
                        style="max-height: calc(100% - 300px); font-family: 'Roboto Mono', sans-serif"
                        class="mdc-dialog__body mdc-dialog__body--scrollable"></pre>
                    <footer class="mdc-dialog__footer">
                        <a kref="$githubLink" target="_blank"><button type="button" class="mdc-button mdc-dialog__footer__button">Show on Github</button></a>
                        <button type="button" class="mdc-button mdc-dialog__footer__button mdc-dialog__footer__button--accept">Close</button>
                    </footer>
                </div>
                <div class="mdc-dialog__backdrop"></div>
            </aside>
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
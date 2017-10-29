package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.components.StackBasedRoutableComponentField
import xyz.nulldev.kdom.api.routing.NOOPRouteHandler
import xyz.nulldev.kdom.api.routing.RouteContext
import xyz.nulldev.kdom.examples.DemoApplication
import xyz.nulldev.kdom.examples.demo.DemoPage
import kotlin.browser.window

class StackBasedRoutingDemo:
        DemoPage("Stack based routing demo",
                "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/StackBasedRoutingDemo.kt") {

    val stack = field(StackComponent())
    var rootPath = "/stack-based-routing/"

    //language=html
    override fun dom() = """
        <div>
            $stack
        </div>
        """.toDom()

    override suspend fun onCompile() {
        routablePage(".")
    }

    override fun handle(context: RouteContext) {
        stack().handle(context)
    }

    fun routablePage(path: String): Unit = stack().router.componentPath(path, {Component.from {
        val backBtn = htmlElement()
        val nestedPages = componentList<Component>()
        val addNested = htmlElement()

        onCompile = {
            backBtn().onclick = {
                DemoApplication.goToPath("../")
            }

            var i = 0
            addNested().onclick = {
                val name = window.prompt("Enter a name for this page:", "page $i")
                val newPath = path + "/" + name
                routablePage(newPath)
                nestedPages += Component.from {
                    onCompile = {
                        compiledDom.root.onclick = {
                            DemoApplication.goToPath(rootPath + newPath)
                        }
                    }

                    """
              <li class="mdc-list-item">$name</li>
                    """.toDom()
                }
                i++
            }
        }

        //language=html
        """
            <div>
            <button kref="$backBtn" class="mdc-button mdc-button--raised">
              <i class="material-icons mdc-button__icon">arrow_back</i>
              Back
            </button>

            <br>

            <div class="mdc-form-field mdc-form-field--align-end">
          <div class="mdc-textfield">
            <input type="text" class="mdc-textfield__input"
                   placeholder="Enter some text">
            <div class="mdc-textfield__bottom-line"></div>
          </div>
        </div>

        <ul class="mdc-list">
            $nestedPages
            </ul>

            <button kref="$addNested" class="mdc-button mdc-button--raised">
              <i class="material-icons mdc-button__icon">note_add</i>
              Add nested page
            </button>
            </div>
            """.toDom()
    }})
}

class StackComponent: StackBasedRoutableComponentField() {
    override val router = StackRouter(this, {})

    override fun dom() = """
        <div>
            $routedComponent
        </div>
        """.toDom()
}
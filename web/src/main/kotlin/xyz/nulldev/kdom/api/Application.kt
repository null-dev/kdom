package xyz.nulldev.kdom.api

import org.w3c.dom.Document
import xyz.nulldev.kdom.api.routing.NOOPRouteHandler
import xyz.nulldev.kdom.api.routing.RouteContext
import xyz.nulldev.kdom.api.routing.RouteHandler
import kotlin.browser.document
import kotlin.browser.window

abstract class Application {
    open val router: RouteHandler = NOOPRouteHandler()

    abstract val rootComponent: Component

    fun attach(doc: Document) {
        doc.body!!.appendChild(rootComponent.compiledDom.root)
        rootComponent.checkAttached()

        //Attach history
        window.onpopstate = {
            document.location?.let {
                silentGoToPath(it.pathname + it.search)
            }
        }
    }

    fun goToPath(path: String) {
        silentGoToPath(path)
        pushPath(path)
    }

    fun silentGoToPath(path: String) {
        router.handle(RouteContext.from(path, null))
    }

    fun pushPath(path: String) {
        window.history.pushState(null, path, path)
    }
}
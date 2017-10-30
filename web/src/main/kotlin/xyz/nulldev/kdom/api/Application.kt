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

    var lastPath: String? = null

    fun attach(doc: Document) {
        doc.body!!.appendChild(rootComponent.compiledDom.root)
        rootComponent.checkAttached()

        //Attach history
        window.onpopstate = {
            document.location?.let {
                val path = it.pathname + it.search
                silentGoToPath(path)
            }
        }
    }

    fun goToPath(path: String) {
        silentGoToPath(path)
        pushPath(path)
    }

    fun silentGoToPath(path: String) {
        val newPath = relToAbsPath(path)
        router.handle(RouteContext.from(newPath, lastPath))
        lastPath = newPath
    }

    fun pushPath(path: String) {
        val newPath = relToAbsPath(path)
        window.history.pushState(null, newPath, newPath)
        lastPath = newPath
    }

    private fun relToAbsPath(path: String): String {
        if(path.startsWith("/"))
            return path

        val curUrl = "/" + window.location.pathname.removeSuffix("/").removePrefix("/") + "/"
        return curUrl + path
    }
}
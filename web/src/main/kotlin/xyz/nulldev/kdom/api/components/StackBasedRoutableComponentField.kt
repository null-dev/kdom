package xyz.nulldev.kdom.api.components

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.EmptyComponent
import xyz.nulldev.kdom.api.routing.RouteContext
import xyz.nulldev.kdom.api.routing.RouteHandler
import xyz.nulldev.kdom.api.routing.Router
import xyz.nulldev.kdom.util.HUGE_STRING

abstract class StackBasedRoutableComponentField : RoutableComponent() {
    protected val routedComponent = field<Component>(EmptyComponent())

    abstract val router: StackRouter
    var lastPathLength = 0

    override fun handle(context: RouteContext) {
        router.handle(context)
    }

    class StackRouter(val parent: StackBasedRoutableComponentField,
                      dsl: StackRouter.() -> Unit) : RouteHandler {
        private val internalRouter = Router {}
        private val cachedComponents = mutableMapOf<String, Pair<Int, Component>>()
        private val nestedStackRouters = mutableListOf<StackRouter>()

        fun folderPath(path: String, dsl: StackRouter.() -> Unit) {
            val router = StackRouter(parent, dsl)
            nestedStackRouters += router
            internalRouter.path(path, {
                router.handle(it)
            })
        }

        fun componentPath(path: String, dsl: (RouteContext) -> Component?) {
            internalRouter.path(path, {
                val res = cachedComponents[path]?.second ?: dsl(it)?.apply {
                    cachedComponents[path] = Pair(it.currentPath.size, this)
                }
                res?.let { comp ->
                    it.addTag(FINAL_PATH_SIZE_TAG, it.currentPath.size)
                    it.finish() // Finish route
                    parent.routedComponent(comp)
                }
            })
        }

        infix fun String.folder(dsl: StackRouter.() -> Unit) {
            folderPath(this, dsl)
        }

        infix fun String.component(dsl: (RouteContext) -> Component?) {
            componentPath(this, dsl)
        }

        override fun handle(context: RouteContext) {
            internalRouter.handle(context)

            // Clear nested components if route was handled in upper/current paths
            val finalTag: Int? = context.getTag(FINAL_PATH_SIZE_TAG)
            if(finalTag != null){
                if(finalTag <= parent.lastPathLength)
                    parent.router.clearCachedComponentsOverPathsize(finalTag)
                parent.lastPathLength = finalTag
            }
        }

        fun clearCachedComponentsOverPathsize(size: Int) {
            cachedComponents.entries.removeAll {
                it.value.first > size
            }
            nestedStackRouters.forEach {
                it.clearCachedComponentsOverPathsize(size)
            }
        }

        init { dsl() }

        companion object {
            private val FINAL_PATH_SIZE_TAG = "${HUGE_STRING}_PATH_STACK_SIZE"
        }
    }
}

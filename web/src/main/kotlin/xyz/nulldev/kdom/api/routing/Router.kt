package xyz.nulldev.kdom.api.routing

class Router(init: RouterDSL): RouteHandler {
    private val nestedRouters = mutableListOf<Pair<PathDefinition, RouteHandler>>()

    operator fun String.invoke(dsl: ContextAwareRouterDSL)
            = path(this, dsl)

    operator fun String.invoke(router: RouteHandler)
            = path(this, router)

    fun path(path: String, dsl: ContextAwareRouterDSL)
            = path(path, ContextAwareRouterDSLHandler(dsl))

    fun path(path: String, router: RouteHandler) {
        nestedRouters += PathDefinition(path) to router
    }

    override fun handle(context: RouteContext) {
        routers@for((path, route) in nestedRouters) {
            if(context.finished)
                return

            val possibleUrlVars = mutableMapOf<String, String>()
            var toPop = 0

            val curPathIndex = context.path.size - context.relativePath.size

            for(item in path.splitPath) {
                val current = context.path.getOrNull(curPathIndex + toPop)
                    ?: continue@routers // Our current path does not match

                when {
                    //TODO Custom path matcher function support
                    item == ".." -> toPop--
                    item == "." -> {} // Matches anything
                    item == "*" -> toPop++ //Matches anything but also pops it from the queue stack
                    item.startsWith(":") -> {
                        //Matches anything, pops from queue stack and assigns as var
                        toPop++
                        possibleUrlVars.put(item.substring(1), current)
                    }
                    item == current -> toPop++ // Matches current
                    else -> continue@routers // Does not match
                }
            }

            //Check if we popped path the end of the url
            if(curPathIndex + toPop > context.path.size)
                continue@routers

            // Check if not end of path but no slash at end
            if(curPathIndex + toPop != context.path.size)
                if(!path.path.endsWith('/'))
                    continue@routers

            fun pushPopPath(amount: Int) {
                var i = amount
                while(i != 0) {
                    if(i > 0) {
                        context.popRelativePath()
                        i--
                    } else {
                        context.pushRelativePath()
                        i++
                    }
                }
            }

            // Push/pop path and add URL vars
            pushPopPath(toPop)
            possibleUrlVars.forEach { context.pushUrlVar(it.key, it.value) }

            route.handle(context)

            // Push/pop back path and URL vars
            pushPopPath(-toPop)
            (1 .. possibleUrlVars.size).forEach { context.popUrlVar() }
        }
    }

    //Run init at end
    init { init(this) }
}

private class PathDefinition(val path: String) {
    val splitPath by lazy {
        val res = path.split("/").filterNot { it.isBlank() }

        //Allow single blank paths
        if(res.isEmpty())
            listOf("")
        else res
    }
}

internal typealias RouterDSL = Router.() -> Unit
internal typealias ContextAwareRouterDSL = Router.(RouteContext) -> Unit

package xyz.nulldev.kdom.api.routing

class ContextAwareRouterDSLHandler(private val dsl: ContextAwareRouterDSL) : RouteHandler {
    override fun handle(context: RouteContext) {
        Router {
            dsl(context)
        }.handle(context)
    }
}
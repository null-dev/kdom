package xyz.nulldev.kdom.api.components

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.EmptyComponent
import xyz.nulldev.kdom.api.routing.RouteContext
import xyz.nulldev.kdom.api.routing.Router

abstract class RoutableComponentField(val routes: Map<String, Component>,
                                      defRoute: String):
        RoutableComponent() {
    protected val routedComponent = field<Component>(EmptyComponent())

    init {
        handle(RouteContext.from(defRoute, null))
    }

    override fun handle(context: RouteContext) {
        Router {
            routes.forEach { (path, comp) ->
                path {
                    routedComponent(comp)

                    (comp as? RoutableComponent)?.handle(it)
                }
            }
        }.handle(context)
    }
}
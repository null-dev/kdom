package xyz.nulldev.kdom.examples.demo

import xyz.nulldev.kdom.api.components.RoutableComponent
import xyz.nulldev.kdom.api.routing.NOOPRouteHandler
import xyz.nulldev.kdom.api.routing.RouteContext

abstract class DemoPage(val name: String, val source: String): RoutableComponent() {
    override fun handle(context: RouteContext) = NOOPRouteHandler().handle(context)
}
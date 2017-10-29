package xyz.nulldev.kdom.api.routing

interface RouteHandler {
    fun handle(context: RouteContext)
}
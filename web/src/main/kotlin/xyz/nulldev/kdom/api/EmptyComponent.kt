package xyz.nulldev.kdom.api

object EmptyComponent : Component() {
    override fun dom() = "<script></script>".toDom()
}
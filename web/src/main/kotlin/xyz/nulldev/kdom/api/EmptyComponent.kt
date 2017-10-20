package xyz.nulldev.kdom.api

class EmptyComponent : Component() {
    override fun dom() = "<script></script>".toDom()
}
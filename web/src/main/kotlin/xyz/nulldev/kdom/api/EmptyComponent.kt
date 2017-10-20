package xyz.nulldev.kdom.api

class EmptyComponent : Component() {
    override fun dom() = """<span style="display: none"></span>""".toDom()
}
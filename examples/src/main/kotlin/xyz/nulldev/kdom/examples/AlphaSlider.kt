package xyz.nulldev.kdom.examples

import org.w3c.dom.HTMLInputElement
import xyz.nulldev.kdom.api.Component

class AlphaSlider: Component() {
    val opacity = field(1f)
    val slider = element<HTMLInputElement>()

    override fun onAttach() {
        slider().oninput = {
            opacity(slider().value.toInt() / 100f)
        }
    }

    override fun dom() = """
        <div>
            <input type="range" min="1" max="100" value="100" kref="$slider">
            <div style="background-color: red; opacity:$opacity">Drag the slider above to change my opacity!</div>
        </div>
        """.toDom()
}
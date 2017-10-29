package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.examples.demo.DemoPage
import kotlin.js.Math

class DynamicAttributesDemo:
        DemoPage("Dynamic attributes",
                "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/DynamicAttributesDemo.kt") {
    private val elevation = field(0)
    private val opacity = field(1f)

    //language=html
    override fun dom() = """
        <div style="padding-top: 24px;padding-left:8px;padding-right:8px">
            ${field(slider(0, 24) { elevation(it) })}
            <figure class="mdc-elevation--z$elevation" style="padding: 16px">
                <figcaption>${elevation}dp (<code>mdc-elevation--z$elevation</code>)</figcaption>
            </figure>
            ${field(slider(0, 100, 100) { opacity(it / 100f) })}
            <figure class="mdc-elevation--z8" style="padding: 16px;opacity:$opacity;background-color:red">
                <figcaption><b>Opacity: ${opacity.transform { "${Math.round(it * 100)}%" }}</b></figcaption>
            </figure>
        </div>
        """.toDom()

    fun slider(start: Int, end: Int, initial: Int = 0, onUpdate: (Int) -> Unit) = Component.from {
        val sliderElement = htmlElement()
        val slider by lazy {
            js("(function(e){return new mdc.slider.MDCSlider(e)})")(sliderElement())
        }

        onAttach = {
            slider.listen("MDCSlider:input", {
                onUpdate(slider.value)
            })
        }

        //language=html
        """
            <div kref="$sliderElement" class="mdc-slider mdc-slider--discrete mdc-slider--display-markers" tabindex="0" role="slider"
                aria-valuemin="$start" aria-valuemax="$end" aria-valuenow="$initial"
                aria-label="Select Value">
                <div class="mdc-slider__track-container">
                    <div class="mdc-slider__track"></div>
                    <div class="mdc-slider__track-marker-container"></div>
                </div>
                <div class="mdc-slider__thumb-container">
                    <div class="mdc-slider__pin">
                        <span class="mdc-slider__pin-value-marker"></span>
                    </div>
                    <svg class="mdc-slider__thumb" width="21" height="21">
                        <circle cx="10.5" cy="10.5" r="7.875"></circle>
                    </svg>
                    <div class="mdc-slider__focus-ring"></div>
                </div>
            </div>
""".toDom()
    }
}
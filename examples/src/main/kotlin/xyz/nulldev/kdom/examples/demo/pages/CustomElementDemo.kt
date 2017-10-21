package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.CustomElementSpec
import xyz.nulldev.kdom.api.SpecManager
import xyz.nulldev.kdom.api.util.dataValue
import xyz.nulldev.kdom.api.util.emit
import xyz.nulldev.kdom.examples.demo.DemoPage
import kotlin.js.Math

class CustomElementDemo:
        DemoPage("Custom elements", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/CustomElementDemo.kt") {
    private val title = field("World")
    private val button = htmlElement()

    private val elevation = field(0)
    private val opacity = field(1f)

    private val elevationSlider = htmlElement()
    private val opacitySlider = htmlElement()

    init {
        SpecManager.registerSpec(MDCTitleSpec)
        SpecManager.registerSpec(MDCSliderSpec)
    }

    //language=html
    override fun dom() = """
        <div>
            <mdc-title>Hello $title!</mdc-title>
            <button class="mdc-button mdc-button--raised" kref="$button">Click me!</button>

            <div style="padding-top: 24px;padding-left:8px;padding-right:8px">
                <mdc-slider kref="$elevationSlider" min="0" max="24" initial="0"></mdc-slider>
                <figure class="mdc-elevation--z$elevation" style="padding: 16px">
                    <figcaption>${elevation}dp (<code>mdc-elevation--z$elevation</code>)</figcaption>
                </figure>
                <mdc-slider kref="$opacitySlider" min="0" max="100" initial="100"></mdc-slider>
                <figure class="mdc-elevation--z8" style="padding: 16px;opacity:$opacity;background-color:red">
                    <figcaption><b>Opacity: ${opacity.transform { "${Math.round(it * 100)}%" }}</b></figcaption>
                </figure>
            </div>
        </div>
        """.toDom()

    override suspend fun onCompile() {
        button().onclick = {
            title("Universe")
        }
        elevationSlider().oninput = {
            elevation(elevationSlider().dataValue.toInt())
        }
        opacitySlider().oninput = {
            opacity(opacitySlider().dataValue.toFloat() / 100f)
        }
    }
}

val MDCTitleSpec = CustomElementSpec("mdc-title", {
    Component.from {
        //language=html
        """
            <h1 class="mdc-typography--display4">${it.content}</h1>
            """.toDom()
    }
})

val MDCSliderSpec = CustomElementSpec("mdc-slider", {
    Component.from {
        val slider by lazy {
            js("(function(e){return new mdc.slider.MDCSlider(e)})")(it.root())
        }

        onAttach = {
            slider.listen("MDCSlider:input", {
                it.root().dataValue = slider.value
                it.root().emit("input")
            })
        }

        //language=html
        """
            <div class="mdc-slider mdc-slider--discrete mdc-slider--display-markers" tabindex="0" role="slider"
                aria-valuemin="${it.attributes["min"]}" aria-valuemax="${it.attributes["max"]}" aria-valuenow="${it.attributes["initial"]}"
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
})

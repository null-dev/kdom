package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.examples.demo.DemoPage
import xyz.nulldev.kdom.mdcshim.ButtonType
import xyz.nulldev.kdom.mdcshim.MDCSpec
import xyz.nulldev.kdom.mdcshim.TextType

class MDCShimDemo:
        DemoPage("MDCShim examples", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/MDCShimDemo.kt") {
        init {
                // Ensure shim is loaded
                MDCSpec.register()
        }

        private val mdcButtonExamples = ButtonType.values().map(ButtonType::name).flatMap { buttonType ->
                TextType.values().map(TextType::name).map { textType ->
                        //language=html
                        """<mdc-button button-type="$buttonType" text-type="$textType" style="margin:8px">$textType $buttonType BUTTON</mdc-button>"""
                } + "<br>"
        }.joinToString(separator = "")

        //language=html
        override fun dom() = """
            <div>
                $mdcButtonExamples
            </div>
            """.toDom()
}

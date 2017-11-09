package xyz.nulldev.kdom.examples.demo.pages

import azadev.kotlin.css.GRAY
import azadev.kotlin.css.GREEN
import azadev.kotlin.css.backgroundColor
import azadev.kotlin.css.color
import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.CustomElementSpec
import xyz.nulldev.kdom.api.SpecManager
import xyz.nulldev.kdom.api.style
import xyz.nulldev.kdom.examples.demo.DemoPage

class ComplexElementsTests:
        DemoPage("Complex elements testing", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/ComplexElementsTests.kt") {
    init {
        SpecManager.registerSpec(TestSpec)
    }

    private val s = style {
        backgroundColor = GRAY
    }

    //language=html
    override fun dom() = """
        <div>
            <test-spec kstyle="$s">The styles on this custom element are created by merging two kstyles</test-spec>
        </div>
        """.toDom()
}

val TestSpec = CustomElementSpec("test-spec", {
    Component.from {
        val s = style {
            color = GREEN
        }

        //language=html
        """
            <h1 kstyle="$s" class="mdc-typography--display4">${it.content}</h1>
            """.toDom()
    }
})

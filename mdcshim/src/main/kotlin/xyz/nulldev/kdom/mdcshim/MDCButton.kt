package xyz.nulldev.kdom.mdcshim

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.api.CustomElementRequest
import xyz.nulldev.kdom.api.util.from

class MDCButton(private val request: CustomElementRequest) : Component() {
    val buttonType = field(ButtonType.RAISED)
            .from(request.attributes["button-type"]?.transform {
                ButtonType.valueOf(it.trim().toUpperCase())
            })
    val textType = field(TextType.BASELINE)
            .from(request.attributes["text-type"]?.transform {
                TextType.valueOf(it.trim().toUpperCase())
            })
    val href = request.attributes["href"]
    val secondary = field(false).from(request.attributes["secondary"]?.transform(String::toBoolean))

    private val tagName = if(href != null) "a" else "button"

    override suspend fun onCompile() {
        // Add href attribute if exists
        if(href != null)
            compiledDom.root.setAttribute("href", href.value)
    }

    //language=html
    override fun dom() = """
        <$tagName
            class="mdc-button
                   ${buttonType.transform(ButtonType::buttonClass)}
                   ${textType.transform(TextType::textClass)}
                   ${secondary.transform { if(it) "secondary-text-button" else "" }}">
            ${request.content}
        </$tagName>
    """.toDom()
}

enum class ButtonType(val buttonClass: String) {
    TEXT(""),
    RAISED("mdc-button--raised"),
    UNELEVATED("mdc-button--unelevated"),
    STROKED("mdc-button--stroked")
}

enum class TextType(val textClass: String) {
    BASELINE(""),
    COMPACT("mdc-button--compact"),
    DENSE("mdc-button--dense")
}

package xyz.nulldev.kdom.api

import org.w3c.dom.HTMLElement

class CustomElementRequest(val parent: Component,
                           val attributes: Map<String, ReadOnlyField<String>>,
                           val content: ReadOnlyField<CustomElementContent>,
                           val parentNode: HTMLElement,
                           val root: Element<HTMLElement>)

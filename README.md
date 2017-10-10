# kdom
*A fast, modular and boilerplate-free framework to build interactive web applications using Kotlin JS.*

## Demos
Simple demos on how to use *kdom* are available [here](http://static.nulldev.xyz/ci/kdom/src/main/resources/page.html). The demo page itself is written using kdom.

## Examples
```
class Header : Component() {
    val htext = field("World!")
    val btn = htmlElement()

    override fun dom() = """
        <div>
            <h1>Hello $htext</h1>
            <button kref="$btn">Click me!</button>
        </div>
        """.toDom()

    override fun onAttach() {
        btn().onclick = {
            htext("Universe!")
        }
    }
}
```
When you click the button, the title changes!
Let's go through how this all works:
1. *kdom* parses the HTML, leaving replacing variable references with their current values

    *kdom* retains a reference to the locations of variables in the DOM
    
    If a tag has a `kref` attribute, *kdom* removes the attribute and sets the variable in the attribute value to the tag.
2. The DOM is inserted into the page calling `onAttach` which then adds a click handler to the button
3. When the button is pressed, *kdom* jumps to the location of the `htext` variable and replaces it's previous value with "Universe!".

**In summary:**
- You can refer to variables in your HTML and *kdom* will copy any changes made to the variable to the DOM
- You can bind a variable to a tag by adding a `kref` attribute to the tag. You can then use the variable as if it is the tag itself!

### Variables in attributes
```
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
```
You can refer to variables in attributes as well.

### Lists of variables
```
class ListExample: Component() {
    val listItems = componentList<ListItem>()
    val addButton = element<HTMLButtonElement>()

    override fun onAttach() {
        var lastIndex = 0
        addButton().onclick = {
            listItems.add(ListItem(++lastIndex, this))
        }
    }

    override fun dom() = """
        <div>
            $listItems
            <button kref="$addButton">Add item</button>
        </div>
        """.toDom()
}
class ListItem(val index: Int, val parent: ListExample): Component() {
    private val removeButton = element<HTMLButtonElement>()

    override fun onAttach() {
        removeButton().onclick = {
            parent.listItems.remove(this)
        }
    }

    override fun dom() = """
        <div>$index: I am a list item! <button kref="$removeButton">Remove</button></div>
        """.toDom()
}
```
*kdom* supports variable lists.
### Manual updating
```
val htext = field(mutableListOf("Hello", "World"))
htext()[1] = "Universe"
htext.update()
```
*kdom* cannot observe mutable objects for changes, but you can manually call `update` to notify *kdom* that a variable has changed.

#### Refer to the [examples project](https://github.com/null-dev/kdom/tree/master/examples) for more examples!

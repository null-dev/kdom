package xyz.nulldev.kdom.examples.demo.pages

import xyz.nulldev.kdom.api.Component
import xyz.nulldev.kdom.examples.demo.DemoPage

class TableDemo :
        DemoPage("Table test", "examples/src/main/kotlin/xyz/nulldev/kdom/examples/demo/pages/TableDemo.kt") {
    private val addColumnButton = htmlElement()
    private val addRowButton = htmlElement()
    private val tableRows = componentList<Component>()
    private val columnCount = field(0)

    //language=html
    override fun dom() = """
        <div>
            <button class="mdc-button mdc-button--raised" kref="$addRowButton">Add row</button>
            <button class="mdc-button mdc-button--raised" kref="$addColumnButton">Add column</button>

            <k-table>
                $tableRows
            </k-table>

            ${field(PrefilledTableTest())}
        </div>
        """.toDom()

    override suspend fun onCompile() {
        addRowButton().onclick = {
            tableRows += tableRow()
            null
        }
        addColumnButton().onclick = {
            columnCount.v++
        }
    }

    fun tableColumn() = Component.from {
        //language=html
        """
            <td>Hi</td>
            """.toDom()
    }

    fun tableRow() = Component.from {
        val columns = componentList(*(1 .. columnCount()).map {
            tableColumn()
        }.toTypedArray())
        var lastCount = columnCount()

        columnCount.addUpdateListener {
            if(columnCount() > lastCount)
                columns += tableColumn()
            else columns.removeAt(columns.lastIndex)

            lastCount = columnCount()
        }

        //language=html
        """
            <k-tr>
                $columns
            </k-tr>
            """.toDom()
    }
}

private class PrefilledTableTest(): Component() {
    //language=html
    override fun dom() = """
        <k-table>
            <k-tr>
                <k-th>Heading 1</k-th>
                <k-th>Heading 2</k-th>
                <k-th>Heading 3</k-th>
            </k-tr>
            <k-tr>
                <k-td>Data 1</k-td>
                <k-td>Data 2</k-td>
                <k-td>Data 3</k-td>
            </k-tr>
            <k-tr>
                <k-td>Data 1</k-td>
                <k-td>Data 2</k-td>
                <k-td>Data 3</k-td>
            </k-tr>
            <k-tr>
                <k-td>Data 1</k-td>
                <k-td>Data 2</k-td>
                <k-td>Data 3</k-td>
            </k-tr>
        </k-table>
        """.toDom()
}
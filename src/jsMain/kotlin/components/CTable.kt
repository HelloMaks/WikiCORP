package components

import csstype.*
import data.InnerTable
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input

external interface TableProps : Props {
    var id: Int
    var innerTable: InnerTable
    var updateTable: (InnerTable) -> Unit
}

val CTable = FC<TableProps> { props ->
    val words: Array<String> = arrayOf("A", "B", "C", "D", "E", "F", "G", "H") // Буквы для таблиц

    props.innerTable.table.let { innerTable ->
        val coordX = innerTable[0].size // Строки (X)
        val coordY = innerTable.size // Столбцы (Y)

        div {
            id = "table_${props.id}"
            repeat(coordY) { y ->
                div {
                    repeat(coordX) { x ->
                        input {
                            value = innerTable[y][x]; maxLength = 10
                            placeholder = "${words[x]}${y + 1}"
                            onChange = { event ->
                                props.updateTable(
                                    InnerTable(
                                        innerTable.mapIndexed { index, list ->
                                            if(index == y) list.mapIndexed { idx, value ->
                                                if(idx == x) event.target.value else value
                                            } else list
                                        }
                                    )
                                )
                            }
                            css {
                                width = 100.pct; height = 3.vh
                                if(x != coordX - 1) marginRight = 0.25.vh
                                if(y != coordY - 1) marginBottom = 0.25.vh
                                border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                fontWeight = FontWeight.bold; fontSize = 24.px; fontFamily = FontFamily.sansSerif
                            }
                        }
                    }
                    css {
                        display = Display.flex; justifyContent = JustifyContent.center; width = (12.5 * coordX).pct
                    }
                }
            }
            arrayOf("+", "-").forEachIndexed { idx, action ->
                button {
                    b { + action; css { fontSize = 24.px } }
                    onClick = {
                        if(idx == 0 && coordX < 8)
                            props.updateTable(InnerTable(innerTable.map { it + "" }))
                        else if(idx == 1 && coordX > 2)
                            props.updateTable(InnerTable(innerTable.map { it.subList(0, it.lastIndex) }))
                    }
                    css {
                        height = 100.pct; width = 1.5.vw; position = Position.absolute
                        backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                        border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                        hover { backgroundColor = Color("#777777") }
                        active { backgroundColor = Color("#444444") }
                        marginLeft = (2.75 + 4 * idx + 12.5 * coordX).pct
                    }
                }
            }
            css {
                display = Display.flex; flexDirection = FlexDirection.column
                alignItems = AlignItems.center; position = Position.relative
            }
        }
        div {
            arrayOf("+", "-").forEachIndexed { idx, action ->
                button {
                    b { + action; css { fontSize = 24.px } }
                    onClick = {
                        if(idx == 0) props.updateTable(InnerTable(innerTable + listOf(List(coordX) { "" })))
                        else if(coordY > 2)
                            props.updateTable(InnerTable(innerTable.subList(0, innerTable.lastIndex)))
                    }
                    css {
                        height = 3.25.vh; width = (0.125 * (coordX - 1) + 6.125 * coordX).pct
                        margin = Margin(0.25.vh, (0.125 * ((idx + 1) % 2)).pct,
                            (0.25 * idx).vh, (0.125 * (idx % 2)).pct)
                        backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                        border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                        hover { backgroundColor = Color("#777777") }
                        active { backgroundColor = Color("#444444") }
                    }
                }
                css { display = Display.flex; justifyContent = JustifyContent.center }
            }
        }
    }
}
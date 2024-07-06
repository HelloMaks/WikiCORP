package components

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.input
import web.html.InputType

external interface TitleProps : Props {
    var id: Int
    var title: String?
}

val CTitle = FC<TitleProps> { props ->
    input {
        id = "title_${props.id}"; type = InputType.text
        props.title?.let { defaultValue = it }; placeholder = "Введите заголовок..."
        css {
            height = 5.vh; width = 100.pct; textAlign = TextAlign.center
            fontSize = 32.px; fontWeight = FontWeight.bold
            overflow = Overflow.hidden; border = 0.px
            outline = 0.px; margin = Margin(1.vh, 0.vw, 1.vh)
        }
    }
}

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.div

external interface MainTitleProps : Props {
    var title: String
}

val CMainTitle = FC<MainTitleProps> { props ->
    div {
        b { + props.title; css { fontSize = 36.px } }
        css {
            display = Display.flex
            margin = Margin(1.75.vh, 0.vw)
            justifyContent = JustifyContent.center
            textDecoration = TextDecoration.underline
            textDecorationThickness = 2.px
        }
    }
}
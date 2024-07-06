package auth

import access.User
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button

external interface AuthOutProps : Props {
    var user: User
    var signOff: () -> Unit
}

val CAuthOut = FC<AuthOutProps> { props ->
    button {
        b { + "🚪"; css { fontSize = 16.px } }
        title = "Выход из аккаунта"
        onClick = { props.signOff() }
        css {
            height = 5.vh; width = 2.5.vw
            backgroundColor = Color("#FFFFFF")
            border = Border(2.px, LineStyle.solid, Color("#FFFFFF")); borderRadius = 50.pct
            hover { borderColor = Color("#000000") }
            active { backgroundColor = Color("#BBBBBB") }
            bottom = 42.vh; left = 0.3.vw; textAlign = TextAlign.center
            position = Position.fixed; zIndex = 2.unsafeCast<ZIndex>()
        }
    }
}
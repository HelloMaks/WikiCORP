package auth

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.useState
import web.html.InputType

external interface AuthInProps : Props {
    var signIn: (Pair<String, String>) -> Unit
    var error: Boolean
}

val CAuthIn = FC<AuthInProps> { props ->
    var login by useState("") // Логин пользователя
    var pass by useState("") // Пароль пользователя
    var show by useState(false) // Регистрация события "Показать пароль"

    val inputColor = if(!props.error) Color("#444444")
        else Color("#ff0000")
    div {
        b { + "CORP`s WIKI"; css { fontSize = 64.px } }
        div {
            h1 { + "Вход на сайт" }
            b { + "Логин"; css { fontSize = 18.px } }
            div {
                input {
                    type = InputType.text
                    value = login
                    onChange = { login = it.target.value }
                    css {
                        outline = 0.px; width = 12.vw; fontWeight = FontWeight.bold
                        border = Border(2.px, LineStyle.solid, inputColor); borderRadius = 5.px
                        focus { boxShadow = BoxShadow(0.px, 0.px, 1.px, 1.px, inputColor) }
                    }
                }
                css { height = 3.vh }
            }
            b { + "Пароль"; css { fontSize = 18.px } }
            div {
                input {
                    value = pass
                    type = if(!show) InputType.password
                    else InputType.text
                    onChange = { pass = it.target.value }
                    css {
                        outline = 0.px; width = 12.vw; fontWeight = FontWeight.bold
                        border = Border(2.px, LineStyle.solid, inputColor); borderRadius = 5.px
                        focus { boxShadow = BoxShadow(0.px, 0.px, 1.px, 1.px, inputColor) }
                    }
                }
                css { height = 3.vh }
            }
            div {
                input {
                    value = show
                    type = InputType.checkbox
                    onChange = { show = !show }
                }
                b { + "Показать пароль"; css { fontSize = 18.px } }
                if(!props.error) css { height = 3.vh }
            }
            if(props.error) {
                div {
                    b { + "Неверный логин или пароль"; css { fontSize = 16.px; color = inputColor } }
                    css { width = 12.vw; margin = Margin(0.5.vh, 0.vw, 0.5.vh); textAlign = TextAlign.center }
                }
            }
            div {
                button {
                    b { + "Вход"; css { fontSize = 16.px } }
                    onClick = { props.signIn(Pair(login, pass)) }
                    css {
                        height = 4.vh; width = 12.35.vw
                        backgroundColor = Color("#333333")
                        color = Color("#FFFFFF"); textAlign = TextAlign.center
                        border = 0.px; borderRadius = 5.px
                        hover {
                            backgroundColor = Color("#222222")
                            boxShadow = BoxShadow(0.px, 4.px, 16.px, 4.px,
                                rgba(0, 0, 0, 0.24))
                        }
                        active { backgroundColor = Color("#555555") }
                    }
                }
            }
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                border = Border(3.px, LineStyle.solid, Color("#000000")); borderRadius = 5.px
                boxShadow = BoxShadow(0.px, 8.px, 12.px, 4.px,
                    rgba(0, 0, 0, 0.24))
                height = 30.vh; width = 40.vw
                margin = Margin(0.vh, 30.vw, 0.vh)
                overflow = Overflow.hidden
            }
        }
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            alignItems = AlignItems.center
            justifyContent = JustifyContent.center
            margin = Margin(35.vh, 0.vw, 0.vh)
        }
    }
}
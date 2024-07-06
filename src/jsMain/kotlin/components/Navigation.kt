package components

import authorization
import csstype.*
import data.rightAdmin
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.router.useNavigate
import react.useContext
import userInfoContext

val CNavigation = FC<Props> {
    val navigate = useNavigate() // Дополнительный контроль навигации в веб-приложении
    val rights = useContext(userInfoContext)!!.first.rights // Права текущего пользователя

    div {
        b { + "CORP`s WIKI"; onClick = { navigate ("") }; css { fontSize = 48.px } }
        css { margin = Margin(5.vh, 4.vw, 0.vh) }
    }
    div {
        arrayOf("Все страницы", "🔎", "+", "💻").forEachIndexed { idx, action ->
            if(idx != 3 || authorization(rights, rightAdmin)) {
                button {
                    b { + action; css { fontSize = 16.px } }
                    when(idx) {
                        1 -> title = "Поиск"
                        2 -> title = "Создать страницу"
                        3 -> title = "Административная панель"
                    }
                    onClick = {
                        when(idx) {
                            0 -> navigate("pages")
                            1 -> navigate("search")
                            2 -> navigate("pages/add")
                            3 -> navigate("adminPanel")
                        }
                    }
                    css {
                        height = 3.vh
                        if(idx != 0) marginLeft = 0.5.vw
                        textAlign = TextAlign.center
                        backgroundColor = Color("#FFFFFF")
                        border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                        hover { backgroundColor = Color("#777777") }
                        active { backgroundColor = Color("#444444") }
                    }
                }
            }
        }
        css {
            display = Display.flex; flexDirection = FlexDirection.row
            alignItems = AlignItems.center; margin = Margin(0.vh, 4.vw)
        }
    }
}
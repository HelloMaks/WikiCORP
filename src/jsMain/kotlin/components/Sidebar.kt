package components

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.router.useNavigate

val CSidebar = FC<Props> {
    val navigate = useNavigate() // Дополнительный контроль навигации в веб-приложении

    div {
        arrayOf("📖", "🔎").forEachIndexed { idx, action ->
            button {
                b { + action; css { fontSize = 16.px } }
                when(idx) {
                    0 -> title = "Главная страница"
                    1 -> title = "Поиск"
                }
                onClick = {
                    when(idx) {
                        0 -> navigate("")
                        1 -> navigate("search")
                    }
                }
                css {
                    height = 5.vh; width = 2.5.vw
                    textAlign = TextAlign.center
                    backgroundColor = Color("#FFFFFF")
                    border = Border(2.px, LineStyle.solid,
                        Color("#FFFFFF")); borderRadius = 50.pct
                    hover { borderColor = Color("#000000") }
                    active { backgroundColor = Color("#BBBBBB") }
                    margin = Margin((40 * ((idx + 1) % 2) + 1.5 * idx).vh, 0.3.vw, 0.vh)
                }
            }
        }
        css {
            width = 3.vw
            position = Position.fixed
            bottom = 0.px; top = 0.px; left = 0.px
            boxShadow = BoxShadow(0.px, 4.px, 12.px, 0.px,
                rgba(0, 0, 0, 0.24))
        }
    }
}
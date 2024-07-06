package components

import CMainTitle
import csstype.*
import emotion.react.css
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useContext
import react.useState
import routes.Routes
import tools.fetch
import userInfoContext
import web.html.InputType
import kotlin.js.json

external interface SearchProps : Props {
    var navigate: (String) -> Unit
}

val CSearch = FC<SearchProps> { props ->
    val userInfo = useContext(userInfoContext) // Информация о текущем пользователе и токине доступа
    var text by useState("") // Текст поиска
    var pageTitles by useState(arrayOf<String>()) // Заголовки найденных страниц

    CMainTitle { this.title = "Поиск" }
    div {
        input {
            type = InputType.text
            placeholder = "Поиск страниц в CORP`s Wiki..."
            value = text; onChange = { text = it.target.value }
            css {
                height = 4.vh; width = 94.pct
                fontSize = 28.px; fontWeight = FontWeight.bold
                overflow = Overflow.hidden
                border = 0.px; outline = 0.px
                borderBottom = Border(2.px, LineStyle.solid)
                placeholder { fontStyle = FontStyle.italic }
            }
        }
        button {
            b { + "🔎"; css { fontSize = 16.px } }
            title = "Начать поиск"
            onClick = {
                if(text.trim { it <= ' ' }.isNotEmpty()) {
                    fetch(
                        "${Routes.pagesPath}/search",
                        jso {
                            method = "POST"
                            headers = json(
                                "Content-Type" to "application/json",
                                "Authorization" to (userInfo?.second?.authHeader ?: "")
                            )
                            body = text
                        }
                    ).then { it.text() }
                        .then { pageTitles = try { Json.decodeFromString(it) } catch(e: Throwable) { emptyArray() } }
                } else pageTitles = emptyArray()
            }
            css {
                height = 4.vh; width = 4.pct
                marginLeft = 1.pct
                backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                hover { backgroundColor = Color("#777777") }
                active { backgroundColor = Color("#444444") }
            }
        }
        css { marginBottom = 3.vh }
    }
    if(pageTitles.isNotEmpty()) {
        pageTitles.forEach { title ->
            div {
                b { + "•"; css { fontSize = 28.px; marginRight = 0.5.pct } }
                b { + title; onClick = { props.navigate(title) }
                    css {
                        fontSize = 28.px; color = Color("#C71585")
                        hover { textDecoration = TextDecoration.underline; textDecorationThickness = 2.px }
                    }
                }
            }
        }
    }
}
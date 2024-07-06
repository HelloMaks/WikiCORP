package components

import csstype.*
import emotion.react.css
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.useContext
import react.useState
import routes.Routes
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

external interface AddPagesMenuProps : Props {
    var navigate: (String) -> Unit
}

val CAddPagesMenu = FC<AddPagesMenuProps> { props ->
    var addPage by useState(false) // Регистрация события создания страницы

    val userInfo = useContext(userInfoContext) // Информация о текущем пользователе и токине доступа
    val queryClient = useQueryClient() // Для управления запросами на мутацию
    val queryPageKey = arrayOf("pages", (userInfo?.first?.login ?: "")).unsafeCast<QueryKey>()

    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText(
                "${Routes.pagesPath}/titles",
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = (userInfo?.first?.login ?: "")
                }
            )
        }
    )

    val deleteMutation = useMutation<HTTPResult, Any, String, Any>(
        mutationFn = { mainTitle: String ->
            fetch(
                "${Routes.pagesPath}/forUsers",
                jso {
                    method = "DELETE"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = Json.encodeToString(Pair(mainTitle, (userInfo?.first?.login ?: "")))
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(queryPageKey)
            }
        }
    )

    if(addPage) { CAddPagesContainer { this.navigate = { props.navigate(it) } } }
    else {
        val statuses = arrayOf("Черновики", "Не приняты", "В процессе", "На проверке")
        if(!query.isLoading && !query.isError) {
            val pageTitles: Array<Array<String>> = try {
                Json.decodeFromString(query.data ?: "")
            } catch (e: Throwable) { emptyArray() }

            if(pageTitles.isEmpty()) addPage = true
            else {
                pageTitles.forEachIndexed { idx, titles ->
                    div {
                        b { + statuses[idx]; css { fontSize = 32.px } }
                        css {
                            height = 5.vh; width = 100.pct
                            display = Display.flex
                            textAlign = TextAlign.center
                            margin = Margin(1.vh, 0.vw, 1.5.vh)
                            justifyContent = JustifyContent.center
                        }
                    }
                    if(titles.isNotEmpty()) {
                        titles.forEach { mainTitle ->
                            div {
                                b { + "•"; css { fontSize = 28.px; marginRight = 0.5.pct } }
                                b { + mainTitle
                                    onClick = {
                                        props.navigate(
                                            when(idx) {
                                                0 -> "drafts"
                                                1 -> "rejects"
                                                2 -> "edit"
                                                else -> "checks"
                                            } + "/${mainTitle}"
                                        )
                                    }
                                    css {
                                        fontSize = 28.px; color = Color("#C71585")
                                        hover {
                                            textDecoration = TextDecoration.underline
                                            textDecorationThickness = 2.px
                                        }
                                    }
                                }
                                button {
                                    b { + "🚮"; css { fontSize = 16.px } }
                                    title = "Удалить страницу"
                                    onClick = { deleteMutation.mutateAsync(mainTitle, null) }
                                    css {
                                        height = 3.vh
                                        backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                                        border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                        hover { backgroundColor = Color("#777777") }
                                        active { backgroundColor = Color("#444444") }
                                        alignSelf = AlignSelf.center; marginLeft = 0.5.pct
                                    }
                                }
                            }
                        }
                    }
                    else {
                        div {
                            b { + "..."; css { fontSize = 28.px } }
                            css {
                                display = Display.flex
                                justifyContent = JustifyContent.center
                                margin = Margin(1.vh, 0.vw, 1.vh)
                            }
                        }
                    }
                }
                div {
                    button {
                        b { + "Создать страницу"; css { fontSize = 16.px } }
                        css {
                            height = 4.vh; width = 25.pct
                            textAlign = TextAlign.center
                            backgroundColor = Color("#FFFFFF")
                            border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                            hover { backgroundColor = Color("#777777") }
                            active { backgroundColor = Color("#444444") }
                        }
                        onClick = { addPage = true }
                    }
                    css {
                        width = 100.pct; display = Display.flex
                        justifyContent = JustifyContent.center
                    }
                }
            }
        }
    }
}
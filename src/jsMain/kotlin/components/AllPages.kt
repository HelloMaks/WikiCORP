package components

import CMainTitle
import authorization
import csstype.*
import data.rightAdmin
import emotion.react.css
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.router.useNavigate
import react.useContext
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

val CAllPages = FC<Props> {
    val userInfo = useContext(userInfoContext) // Информация о текущем пользователе и токине доступа

    val navigate = useNavigate() // Дополнительный контроль навигации в веб-приложении
    val queryClient = useQueryClient() // Для управления запросами на мутацию
    val queryPageKey = arrayOf("pages").unsafeCast<QueryKey>() // Ключ для запроса на сервер

    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText(
                "${Routes.pagesPath}/titles",
                jso {
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                }
            )
        }
    )

    val deleteMutation = useMutation<HTTPResult, Any, String, Any>(
        mutationFn = { mainTitle: String ->
            fetch(
                Routes.pagesPath,
                jso {
                    method = "DELETE"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = mainTitle
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(queryPageKey)
            }
        }
    )

    CMainTitle { this.title = "Все страницы" }
    if(!query.isLoading && !query.isError) {
        val pageTitles: Array<String> = try {
            Json.decodeFromString(query.data ?: "")
        } catch (e: Throwable) { emptyArray() }

        if(pageTitles.isNotEmpty()) {
            pageTitles.forEach { mainTitle ->
                div {
                    b { + "•"; css { fontSize = 28.px; marginRight = 0.5.pct } }
                    b { + mainTitle; onClick = { navigate(mainTitle) }
                        css {
                            fontSize = 28.px; color = Color("#C71585")
                            hover { textDecoration = TextDecoration.underline; textDecorationThickness = 2.px }
                        }
                    }
                    if(authorization((userInfo?.first?.rights ?: emptyArray()), rightAdmin)) {
                        button {
                            b { + "🚮"; css { fontSize = 16.px } }
                            title = "Удалить страницу"
                            onClick = { deleteMutation.mutateAsync(mainTitle, null) }
                            css {
                                height = 3.vh
                                textAlign = TextAlign.center
                                backgroundColor = Color("#FFFFFF")
                                border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                hover { backgroundColor = Color("#777777") }
                                active { backgroundColor = Color("#444444") }
                                alignSelf = AlignSelf.center; marginLeft = 0.5.pct
                            }
                        }
                    }
                }
            }
        }
    }
}
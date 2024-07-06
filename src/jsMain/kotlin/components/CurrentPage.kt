package components

import CMainTitle
import authorization
import csstype.*
import data.Status
import data.WikiPage
import data.rightAdmin
import emotion.react.css
import js.core.get
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
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.textarea
import react.router.useParams
import react.useContext
import react.useState
import routes.Routes
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

external interface CurrentPageProps : Props {
    var status: String
    var navigate: (String) -> Unit
}

val CCurrentPage = FC<CurrentPageProps> { props ->
    var rejected by useState(false) // Регистрация события "Отказать"
    var reason by useState("") // Причина отказа

    val title = useParams()["title"] ?: "" // Заголовок страницы
    val status: Status = when (props.status) {
        "draft" -> Status.Draft
        "check" -> Status.Check
        "reject" -> Status.Rejected
        else -> Status.Ready
    } // Статус страницы

    val userInfo = useContext(userInfoContext)
    val queryPageKey = arrayOf("pages", title).unsafeCast<QueryKey>() // Ключ для запроса на сервер

    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText(
                "${Routes.pagesPath}/$title",
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = Json.encodeToString(Pair((userInfo?.first?.login ?: ""), status))
                }
            )
        }
    )

    val updateMutation = useMutation<HTTPResult, Any, Pair<String, Status>, Any>(
        mutationFn = { (reason, newStatus) ->
            fetch(
                "${Routes.pagesPath}/$title",
                jso {
                    method = "PUT"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = Json.encodeToString(Pair(reason, newStatus))
                }
            )
        },
        options = jso {
            onSuccess = { result: HTTPResult, (_, newStatus), _: Any? ->
                result.text().then {
                    when(newStatus) {
                        Status.Ready -> {
                            if(rejected) rejected = false
                            props.navigate(title)
                        }
                        Status.Rejected -> {
                            rejected = false
                            props.navigate("rejects/$title")
                        }
                        else -> {
                            rejected = false
                            props.navigate("edit/$title")
                        }
                    }
                }
            }
        }
    )

    if(!query.isLoading && !query.isError) {
        val page: WikiPage? = try { Json.decodeFromString(query.data ?: "") } catch (e: Throwable) { null }
        page?.let { wikiPage ->
            CMainTitle { this.title = wikiPage.mainTitle }
            wikiPage.contents.forEach { content ->
                content.text?.let {
                    if(it.text.trim { char -> char <= ' ' }.isNotEmpty()) {
                        textarea {
                            value = it.text; readOnly = true
                            css {
                                resize = "none".unsafeCast<Resize>(); height = it.height.px; width = 100.pct
                                fontFamily = FontFamily.sansSerif; fontSize = 28.px; fontWeight = FontWeight.bold
                                overflow = Overflow.hidden; border = 0.px; outline = 0.px; marginBottom = 0.5.vh
                            }
                        }
                    }
                }
                content.title?.let {
                    if(it.title.trim { char -> char <= ' ' }.isNotEmpty()) {
                        div {
                            b { + it.title; css { fontSize = 32.px } }
                            css {
                                display = Display.flex; height = 5.vh; width = 100.pct
                                textAlign = TextAlign.center; margin = Margin(1.vh, 0.vw, 1.vh)
                                alignItems = AlignItems.center; justifyContent = JustifyContent.center
                                marginBottom = 0.5.vh
                            }
                        }
                    }
                }
                content.image?.let {
                    div {
                        img {
                            src = it.src
                            height = it.sizes[1]; width = it.sizes[0]
                            css { maxWidth = 100.pct }
                        }
                        if(it.sign.trim { char -> char <= ' ' }.isNotEmpty()) {
                            b {
                                + it.sign
                                css {
                                    textAlign = TextAlign.center; width = 100.pct
                                    fontSize = 24.px; fontWeight = FontWeight.bold
                                }
                            }
                        }
                        css {
                            display = Display.flex; flexDirection = FlexDirection.column
                            alignItems = AlignItems.center; justifyContent = JustifyContent.center
                            marginBottom = 0.5.vh
                        }
                    }
                }
                content.table?.let {
                    val coordX = it.values.table[0].size // Строки (X)
                    val coordY = it.values.table.size // Столбцы (Y)
                    div {
                        repeat(coordY) { y ->
                            div {
                                repeat(coordX) { x ->
                                    input {
                                        value = it.values.table[y][x]; readOnly = true
                                        css {
                                            height = 3.vh; width = 100.pct
                                            if (y != coordY - 1) marginBottom = 0.25.vh
                                            border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                            fontWeight = FontWeight.bold; fontSize = 24.px
                                        }
                                    }
                                }
                                css {
                                    display = Display.flex
                                    justifyContent = JustifyContent.center; width = (10 * coordX).pct
                                }
                            }
                        }
                        css {
                            display = Display.flex; flexDirection = FlexDirection.column
                            alignItems = AlignItems.center; marginBottom = 0.5.vh
                        }
                    }
                }
            }
            if(rejected || status == Status.Rejected) {
                CTextArea {
                    this.reason = if(status != Status.Rejected) reason else wikiPage.reason
                    this.updReason = { reason = it }
                }
            }
            if(status != Status.Ready) {
                if(status in arrayOf(Status.Draft, Status.Rejected) ||
                    (userInfo!!.first.login == wikiPage.author && status == Status.Check)) {
                    div {
                        button {
                            b { + "Внести правки"; css { fontSize = 16.px } }
                            css {
                                height = 4.vh; width = 25.pct
                                backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                                border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                hover { backgroundColor = Color("#777777") }
                                active { backgroundColor = Color("#444444") }
                            }
                            onClick = { updateMutation.mutateAsync(Pair("", Status.InProgress), null) }
                        }
                        css {
                            width = 100.pct; display = Display.flex
                            justifyContent = JustifyContent.center; marginBottom = 0.5.vh
                        }
                    }
                }
                if(authorization((userInfo?.first?.rights ?: emptyArray()), rightAdmin) && status == Status.Check) {
                    div {
                        arrayOf("Принять", "Отказать").forEachIndexed { idx, action ->
                            button {
                                b { + action; css { fontSize = 16.px } }
                                css {
                                    height = 4.vh; width = 12.375.pct
                                    backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                                    border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                    hover { backgroundColor = Color("#777777") }
                                    active { backgroundColor = Color("#444444") }
                                    if(idx == 0) marginRight = 0.125.pct
                                    if(idx == 1) marginLeft = 0.125.pct
                                }
                                onClick = {
                                    if(idx == 0) updateMutation.mutateAsync(Pair("", Status.Ready), null)
                                    else if(!rejected) rejected = true
                                    else if(rejected)
                                        updateMutation.mutateAsync(Pair(reason, Status.Rejected), null)
                                }
                            }
                        }
                        css {
                            width = 100.pct; display = Display.flex
                            justifyContent = JustifyContent.center
                        }
                    }
                }
            }
        } ?: CMainTitle { this.title = "404 СТРАНИЦА НЕ НАЙДЕНА!" }
    }
}
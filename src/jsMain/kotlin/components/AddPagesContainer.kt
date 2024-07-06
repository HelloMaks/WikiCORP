package components

import CMainTitle
import data.Status
import data.WikiPage
import data.json
import js.core.get
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
import react.router.useParams
import react.useContext
import routes.Routes
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json


external interface AddPagesContainerProps : Props {
    var navigate: (String) -> Unit
    var status: Status?
}

val CAddPagesContainer = FC<AddPagesContainerProps> { props ->
    val userInfo = useContext(userInfoContext) // Информация о текущем пользователе и токине доступа
    val title = useParams()["title"] ?: "" // Заголовок страницы
    val queryPageKey = arrayOf("pages", title).unsafeCast<QueryKey>() // Ключ для запроса на сервер

    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText("${Routes.pagesPath}/$title",
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = Json.encodeToString(Pair((userInfo?.first?.login ?: ""), props.status))
                }
            )
        }
    )

    val addMutation = useMutation<HTTPResult, Any, WikiPage, Any>(
        mutationFn = { newPage: WikiPage ->
            fetch(
                Routes.pagesPath,
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to (userInfo?.second?.authHeader ?: "")
                    )
                    body = newPage.json
                }
            )
        },
        options = jso {
            onSuccess = { result: HTTPResult, page: WikiPage, _: Any? ->
                result.text().then {
                    if(page.status == Status.Draft) props.navigate("drafts/${page.mainTitle}")
                    else if(page.status == Status.Check) props.navigate("checks/${page.mainTitle}")
                }
            }
        }
    )

    val updateMutation = useMutation<HTTPResult, Any, WikiPage, Any>(
        mutationFn = { newPage: WikiPage ->
            fetch(
                Routes.pagesPath,
                jso {
                    method = "PUT"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = newPage.json
                }
            )
        },
        options = jso {
            onSuccess = { result: HTTPResult, page: WikiPage, _: Any? ->
                result.text().then {
                    if(page.status == Status.Draft) props.navigate("drafts/${page.mainTitle}")
                    else if(page.status == Status.Check) props.navigate("checks/${page.mainTitle}")
                }
            }
        }
    )

    props.status?.let {
         if(!query.isLoading && !query.isError) {
             val page: WikiPage? = try {
                 Json.decodeFromString<WikiPage>(query.data ?: "")
             } catch(e: Throwable) { null }
             page?.let { wikiPage ->
                 CAddPages { this.mutation = { updateMutation.mutateAsync(it, null) }; this.page = wikiPage }
             } ?: CMainTitle { this.title = "404 СТРАНИЦА НЕ НАЙДЕНА!" }
        }
    } ?: CAddPages { this.mutation = { addMutation.mutateAsync(it, null) } }
}
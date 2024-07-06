package components

import CMainTitle
import access.User
import access.json
import authorization
import data.rightAdmin
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
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

val CAdminPanelContainer = FC<Props> {
    val userInfo = useContext(userInfoContext) // Информация о текущем пользователе и токине доступа
    if(authorization((userInfo?.first?.rights ?: emptyArray()), rightAdmin)) {
        val queryClient = useQueryClient() // Для управления запросами на мутацию
        val queryPageKey = arrayOf("users").unsafeCast<QueryKey>() // Ключ для запроса на сервер

        val query = useQuery<String, QueryError, String, QueryKey>(
            queryKey = queryPageKey,
            queryFn = {
                fetchText(
                    Routes.usersPath,
                    jso {
                        headers = json(
                            "Content-Type" to "application/json",
                            "Authorization" to (userInfo?.second?.authHeader ?: "")
                        )
                    }
                )
            }
        )

        val addMutation = useMutation<HTTPResult, Any, User, Any>(
            mutationFn = { user: User ->
                fetch(
                    Routes.usersPath,
                    jso {
                        method = "POST"
                        headers = json(
                            "Content-Type" to "application/json",
                            "Authorization" to (userInfo?.second?.authHeader ?: "")
                        )
                        body = user.json
                    }
                )
            },
            options = jso {
                onSuccess = { _: Any, _: Any, _: Any? ->
                    queryClient.invalidateQueries<Any>(queryPageKey)
                }
            }
        )

        val deleteMutation = useMutation<HTTPResult, Any, String, Any>(
            mutationFn = { login: String ->
                fetch(
                    Routes.usersPath,
                    jso {
                        method = "DELETE"
                        headers = json(
                            "Content-Type" to "application/json",
                            "Authorization" to (userInfo?.second?.authHeader ?: "")
                        )
                        body = login
                    }
                )
            },
            options = jso {
                onSuccess = { _: Any, _: Any, _: Any? ->
                    queryClient.invalidateQueries<Any>(queryPageKey)
                }
            }
        )

        if(!query.isLoading && !query.isError) {
            val users: Array<User> = try {
                Json.decodeFromString(query.data ?: "")
            } catch (e: Throwable) { emptyArray() }

            CAdminPanel {
                this.users = users
                this.addMutation = { addMutation.mutateAsync(it, null) }
                this.deleteMutation = { deleteMutation.mutateAsync(it, null) }
            }
        }
    }
    else CMainTitle { this.title = "404 СТРАНИЦА НЕ НАЙДЕНА!" }
}
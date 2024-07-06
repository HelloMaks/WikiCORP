package auth

import access.AccRight
import access.Token
import access.User
import access.json
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.useState
import routes.Routes
import tools.fetch
import kotlin.js.json

external interface AuthContainerProps : Props {
    var user: User?
    var signIn: (Pair<User, Token>) -> Unit
    var signOff: () -> Unit
}

val CAuthContainer = FC<AuthContainerProps> { props ->
    var error by useState(false) // Регистрация ошибок при неверном логине или пароле
    props.user?.let { user ->
        if(error) error = false
        CAuthOut {
            this.user = user
            signOff = props.signOff
        }
    } ?:
    CAuthIn {
        signIn = { (login: String, pass: String) ->
            fetch(
                Routes.loginPath,
                jso {
                    method = "POST"
                    headers = json("Content-Type" to "application/json")
                    body = (User(login, pass)).json
                }
            ).then { it.text() }.then {
                val pair: Pair<Array<AccRight>, Token>? = try {
                    Json.decodeFromString(it) } catch(e: Exception) { null }
                if(pair != null) {
                    props.signIn(Pair(User(login, pass, pair.first), pair.second))
                } else error = true
            }
        }
        this.error = error
    }
}
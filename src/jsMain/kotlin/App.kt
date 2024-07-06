
import access.AccRight
import access.Token
import access.User
import auth.authProvider
import components.CNavigation
import components.CSidebar
import components.CWrapperPage
import react.FC
import react.Props
import react.create
import react.createContext
import react.dom.client.createRoot
import react.router.dom.HashRouter
import tanstack.query.core.QueryClient
import tanstack.react.query.QueryClientProvider
import web.dom.document

fun main() {
    val container = document.getElementById("root")!!
    createRoot(container).render(CApp.create())
}

/* Функция проверки прав доступа пользователя */
fun authorization(rights: Array<AccRight>, needRight: AccRight): Boolean =
    rights.any { it.name == needRight.name }

typealias UserInfo = Pair<User, Token>?
val userInfoContext = createContext<UserInfo>(null)

val CApp = FC<Props> {
    authProvider {
        HashRouter {
            QueryClientProvider {
                CSidebar { } // Боковое меню
                CNavigation { } // Верхнее меню
                CWrapperPage { } // Оболочка страниц википедии

                client = QueryClient()
            }
        }
    }
}
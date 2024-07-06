package components

import CMainTitle
import csstype.*
import data.Status
import emotion.react.css
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.Route
import react.router.Routes
import react.router.useNavigate

val CWrapperPage = FC<Props> {
    val navigate = useNavigate() // Дополнительный контроль навигации в веб-приложении
    div {
        /* Пути приложения */
        Routes {
            Route { path = ""; element = CMainPage.create { } }
            Route { path = "pages"; element = CAllPages.create { } }
            Route {
                path = "search"
                element = CSearch.create { this.navigate = { navigate("pages/$it") } }
            }
            Route {
                path = "pages/add"
                element = CAddPagesMenu.create { this.navigate = { navigate("pages/$it") } }
            }
            Route {
                path = "pages/edit/:title"
                element = CAddPagesContainer.create {
                    this.navigate = { navigate("pages/$it") }
                    this.status = Status.InProgress
                }
            }
            Route { path = "adminPanel"; element = CAdminPanelContainer.create {  } }
            arrayOf("draft", "reject", "check", "ready").forEach { status ->
                Route {
                    path = if(status != "ready") "pages/${status}s/:title" else "pages/:title"
                    element = CCurrentPage.create {
                        this.status = status; this.navigate = { navigate("pages/$it") }
                    }
                }
            }
            Route { path = "/*"; element = CMainTitle.create { this.title = "404 СТРАНИЦА НЕ НАЙДЕНА!" } }
        }
        css {
            minHeight = 80.vh
            overflow = Overflow.hidden
            textAlign = TextAlign.justify
            border = Border(2.px, LineStyle.solid); borderRadius = 5.px
            margin = Margin(1.vh, 4.vw, 0.vh); padding = Padding(2.vh, 4.vw)
            boxShadow = BoxShadow(0.px, 0.px, 4.px, 4.px,
                rgba(0, 0, 0, 0.24))
        }
    }
}
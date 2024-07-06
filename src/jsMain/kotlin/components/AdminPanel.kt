package components

import CMainTitle
import access.User
import authorization
import csstype.*
import data.rightAdmin
import data.rightUser
import data.userList
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useState
import web.html.InputType

external interface AdminPanelProps : Props {
    var users: Array<User>
    var addMutation: (User) -> Unit
    var deleteMutation: (String) -> Unit
}

val CAdminPanel = FC<AdminPanelProps> { props ->
    var login by useState("") // Логин пользователя
    var pass by useState("") // Пароль пользователя
    var admin by useState(false) // Наличие прав администратора
    var error by useState(false) // Регистрация ошибки при вводе

    CMainTitle { this.title = "Административная панель" }
    div {
        arrayOf("Добавить пользователя...", "Добавить пароль...").forEachIndexed { idx, placehol ->
            input {
                type = InputType.text
                placeholder = placehol
                value = if(idx == 0) login else pass
                onChange = {
                    if(idx == 0) login = it.target.value
                    else pass = it.target.value
                }
                css {
                    height = 4.vh; width = 44.pct
                    fontSize = 28.px; fontWeight = FontWeight.bold
                    overflow = Overflow.hidden
                    border = 0.px; outline = 0.px
                    if(idx == 0) marginRight = 1.pct
                    borderBottom = if(!error || idx == 1) Border(2.px, LineStyle.solid)
                    else Border(2.px, LineStyle.solid, Color("#ff0000"))
                    placeholder {
                        fontStyle = FontStyle.italic; paddingLeft = 3.px
                        if(error && idx == 0) color = Color("#cd5c5c")
                    }
                }
            }
        }
        arrayOf("👑", "+").forEachIndexed { idx, action ->
            button {
                b { + action; css { fontSize = 16.px } }
                when(idx) {
                    0 -> title = "Назначить администратором"
                    1 -> title = "Добавить пользователя"
                }
                onClick = {
                    if(idx == 0) admin = !admin
                    else {
                        if(login.trim { it <= ' '}.isNotEmpty()) {
                            if(error) error = false
                            if(!admin) props.addMutation(User(login, pass, arrayOf(rightUser)))
                            else props.addMutation(User(login, pass, arrayOf(rightUser, rightAdmin)))
                        }
                        else if(!error) error = true
                    }
                }
                css {
                    height = 4.vh; width = 4.pct
                    textAlign = TextAlign.center
                    backgroundColor = Color("#FFFFFF")
                    border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                    if(idx == 0 && admin) { backgroundColor = Color("#888888") }
                    hover { backgroundColor = Color("#777777") }
                    active { backgroundColor = Color("#444444") }
                    position = Position.fixed
                    marginLeft = if(idx == 0) 0.5.pct else 5.pct
                }
            }
        }
        div {
            b { + "Список пользователей:"; css { fontSize = 28.px } }
            css {
                display = Display.flex; margin = Margin(2.vh, 0.vw, (-1).vh)
                alignItems = AlignItems.center; justifyContent = JustifyContent.center
            }
        }
        if(props.users.isNotEmpty()) {
            props.users.forEach { user ->
                div {
                    b {
                        if(authorization(user.rights, rightAdmin))
                            + "• ${user.login} 👑" else + "• ${user.login}"
                        css { fontSize = 28.px }
                    }
                    arrayOf("🖌", "🚮").forEachIndexed { idx, action ->
                        if(idx == 0 || user.login !in userList.map { it.login }) {
                            button {
                                b { + action; css { fontSize = 16.px } }
                                when(idx) {
                                    0 -> title = "Обновить данные"
                                    1 -> title = "Удалить пользователя"
                                }
                                onClick = {
                                    if(idx == 0) {
                                        login = user.login
                                        pass = user.password
                                        admin = authorization(user.rights, rightAdmin)
                                    } else { props.deleteMutation(user.login) }
                                }
                                css {
                                    height = 3.vh
                                    textAlign = TextAlign.center
                                    backgroundColor = Color("#FFFFFF");
                                    border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                                    hover { backgroundColor = Color("#777777") }
                                    active { backgroundColor = Color("#444444") }
                                    alignSelf = AlignSelf.center; marginLeft = 0.5.pct
                                }
                            }
                        }
                    }
                    css { display = Display.flex; flexDirection = FlexDirection.row }
                }
            }
        }
    }
}
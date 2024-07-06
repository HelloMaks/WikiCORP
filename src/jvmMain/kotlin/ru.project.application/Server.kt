package ru.project.application

import data.userList
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.html.*
import ru.project.application.auth.authConfig
import ru.project.application.auth.authRoutes
import ru.project.application.db.users
import ru.project.application.rest.pageRoutes
import ru.project.application.rest.userRoutes

fun HTML.index() {
    head { title("CORP`s Wiki") }
    body {
        div { id = "root"; b { + "Загрузка..." } }
        script(src = "/static/hellomaks_diplome.js") {}
    }
}

fun main() {
    val environment = applicationEngineEnvironment {
        connector { port = 8080; host = "127.0.0.1" }
        module(Application::main)
    }
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.main() {
    config(); static(); restAPI()
    if(users.find().toList().isEmpty())
        users.insertMany(userList)
}

fun Application.config() {
    install(ContentNegotiation) { json() }; authConfig()
}

fun Application.restAPI() {
    routing {
        authRoutes()
        userRoutes()
        pageRoutes()
    }
}

fun Application.static() {
    routing {
        get("/") { call.respondHtml(HttpStatusCode.OK, HTML::index) }
        static("/static") { resources() }
    }
}
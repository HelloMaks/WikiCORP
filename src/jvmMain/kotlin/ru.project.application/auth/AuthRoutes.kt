package ru.project.application.auth

import access.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import data.rightAdmin
import data.userList
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import routes.Routes
import ru.project.application.auth.AuthInfo.Companion.audience
import ru.project.application.auth.AuthInfo.Companion.issuer
import ru.project.application.auth.AuthInfo.Companion.secret
import ru.project.application.db.users
import java.util.*

fun Route.authRoutes() {
    post(Routes.loginPath) {
        val user: User = call.receive()
        val checkUser: User? = users.find().find { it.login == user.login && it.password == user.password }

        checkUser?.let { checked ->
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("login", checked.login)
                .withExpiresAt(Date(System.currentTimeMillis() + 1800000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(Pair(checked.rights, hashMapOf("token" to token)))
        } ?: return@post call.respondText("Wrong login or password", status = HttpStatusCode.Unauthorized)
    }
    route("check") {
        authenticate("auth-jwt") {
            authorization(rightAdmin) {
                get { call.respondText("Auth OK! You`re verified as Admin!", status = HttpStatusCode.OK) }
            }
        }
    }
}
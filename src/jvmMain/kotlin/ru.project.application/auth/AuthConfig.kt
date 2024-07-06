package ru.project.application.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import ru.project.application.db.users

class AuthInfo {
    companion object {
        const val secret = "secret"
        const val issuer = "http://0.0.0.0:8080/"
        const val audience = "http://0.0.0.0:8080/hello"
        const val myRealm = "Access to 'hello'"
    }
}

fun Application.authConfig() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = AuthInfo.myRealm
            verifier(
                JWT.require(Algorithm.HMAC256(AuthInfo.secret))
                    .withAudience(AuthInfo.audience)
                    .withIssuer(AuthInfo.issuer)
                    .build()
            )
            validate { credential ->
                users.find().find { user ->
                    user.login == credential.payload.getClaim("login").asString()
                }?.let { UserPrincipal(it) }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    install(Authorization) {
        getRights = { user -> user.rights }
    }
}
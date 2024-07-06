package ru.project.application.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

open class AuthException(val _message: String): Exception(_message){
    suspend fun handler(call: ApplicationCall){
        call.respond(HttpStatusCode.Unauthorized, _message)
    }
}

object PrincipalError: AuthException("Missing principal")
object AccessDenied: AuthException("Access is denied")
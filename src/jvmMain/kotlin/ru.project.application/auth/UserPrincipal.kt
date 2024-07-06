package ru.project.application.auth

import access.User
import io.ktor.server.auth.*

class UserPrincipal(val user: User): Principal
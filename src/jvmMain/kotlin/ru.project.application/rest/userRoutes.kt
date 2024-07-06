package ru.project.application.rest

import access.User
import data.rightAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import ru.project.application.auth.authorization
import ru.project.application.db.users

fun Route.userRoutes() {
    route("users") {
        authenticate("auth-jwt") {
            authorization(rightAdmin) {
                get {
                    val usersFromDB = users.find().toList()

                    if(usersFromDB.isNotEmpty()) call.respond(usersFromDB)
                    else call.respondText("Users from MongoDB not found!", status = HttpStatusCode.NotFound)
                }
                post {
                    val user = call.receive<User>()
                    val updates = arrayOf(false, false)

                    users.find().toList().find { it.login == user.login }?.let { check ->
                        if(check.password != user.password) updates[0] = true
                        if(check.rights.map { it.name } != user.rights.map { it.name }) updates[1] = true

                        when {
                            updates.all { it } -> {
                                users.updateOne(User::login eq user.login,
                                    listOf(setValue(User::password, user.password),
                                        setValue(User::rights, user.rights)))
                                return@post call.respondText("The user`s password and rights " +
                                    "were successfully updated!", status = HttpStatusCode.OK)
                            }
                            updates[0] -> {
                                users.updateOne(User::login eq user.login, setValue(User::password, user.password))
                                return@post call.respondText("The user`s password was successfully updated!",
                                    status = HttpStatusCode.OK)
                            }
                            updates[1] -> {
                                users.updateOne(User::login eq user.login, setValue(User::rights, user.rights))
                                return@post call.respondText("The user`s rights were successfully updated!",
                                    status = HttpStatusCode.OK)
                            }
                            else -> return@post call.respondText("The user has already been added to MongoDB!",
                                status = HttpStatusCode.BadRequest)
                        }
                    } ?: run {
                        users.insertOne(user)
                        call.respondText("The user has been added to MongoDB!", status = HttpStatusCode.OK)
                    }
                }
                delete {
                    val login = call.receive<String>()

                    users.findOne(User::login eq login)?.let {
                        users.deleteOne(User::login eq login)
                        call.respondText("The user from MongoDB was successfully deleted!",
                            status = HttpStatusCode.OK)
                    } ?: call.respondText("The user from MongoDB not found!", status = HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
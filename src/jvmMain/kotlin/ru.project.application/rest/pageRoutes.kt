package ru.project.application.rest

import access.User
import data.Status
import data.WikiPage
import data.rightAdmin
import data.rightUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.*
import ru.project.application.auth.authorization
import ru.project.application.db.pages
import ru.project.application.db.users

fun Route.pageRoutes() {
    route("pages") {
        authenticate("auth-jwt") {
            authorization(rightUser) {
                get("titles") {
                    val pageTitles = pages.find().filter { it.status == Status.Ready }.map { it.mainTitle }

                    if(pageTitles.isNotEmpty()) call.respond(pageTitles)
                    else call.respondText("Pages from MongoDB not found!", status = HttpStatusCode.NotFound)
                }
                post("titles") {
                    val userLogin = call.receive<String>()
                    val pageTitles: MutableList<List<String>> = mutableListOf()

                    arrayOf(Status.Draft, Status.Rejected, Status.InProgress, Status.Check).forEach { status ->
                        pageTitles.add(pages.find().filter {
                            it.author == userLogin && it.status == status
                        }.map { it.mainTitle })
                    }
                    if(pageTitles.any { it.isNotEmpty() }) call.respond(pageTitles)
                    else call.respondText("Pages from MongoDB not found!", status = HttpStatusCode.NotFound)
                }
                post {
                    val page = call.receive<WikiPage>()

                    if(pages.find().toList().none { it.mainTitle == page.mainTitle }) {
                        pages.insertOne(page)
                        call.respondText("The page has been added to MongoDB!", status = HttpStatusCode.OK)
                    } else call.respondText("The page has already been added to MongoDB!",
                        status = HttpStatusCode.BadRequest)
                }
                put {
                    val page = call.receive<WikiPage>()

                    pages.find().find { it.mainTitle == page.mainTitle }?.let { wikiPage ->
                        if(wikiPage.status != page.status) {
                            pages.replaceOne(WikiPage::mainTitle eq page.mainTitle, page)
                            call.respondText("The page has been updated to MongoDB!", status = HttpStatusCode.OK)
                        } else call.respondText("The page has already been updated to MongoDB!",
                            status = HttpStatusCode.BadRequest)
                    } ?: call.respondText("The page from MongoDB not found!", status = HttpStatusCode.NotFound)
                }
                put("{title}") {
                    val title = call.parameters["title"] ?: return@put call.respondText(
                        "Missing or malformed title", status = HttpStatusCode.BadRequest)
                    val (reason, status) = call.receive<Pair<String, Status>>()

                    pages.findOne(WikiPage::mainTitle eq title)?.let {
                        pages.updateOne(WikiPage::mainTitle eq title,
                            listOf(setValue(WikiPage::status, status), setValue(WikiPage::reason, reason)))
                        call.respondText("The page from MongoDB has been updated!", status = HttpStatusCode.OK)
                    } ?: call.respondText("The page from MongoDB not found!", status = HttpStatusCode.NotFound)
                }
                post("{title}") {
                    val title = call.parameters["title"] ?: return@post call.respondText(
                        "Missing or malformed title", status = HttpStatusCode.BadRequest)
                    val (userLogin, pageStatus) = call.receive<Pair<String, Status?>>()

                    pageStatus?.let { status ->
                        when(status) {
                            Status.Draft, Status.Rejected, Status.InProgress -> {
                                pages.findOne(WikiPage::mainTitle eq title,
                                    WikiPage::status eq status,
                                    WikiPage::author eq userLogin
                                )?.let { page -> return@post call.respond(page) }
                            }
                            Status.Check -> {
                                users.findOne(User::login eq userLogin)?.let { user ->
                                    if(user.rights.any { right -> right.name == rightAdmin.name }) {
                                        pages.findOne(WikiPage::mainTitle eq title,
                                            WikiPage::status eq Status.Check
                                        )?.let { page -> return@post call.respond(page) }
                                    }
                                    else {
                                        pages.findOne(WikiPage::mainTitle eq title,
                                            WikiPage::status eq Status.Check,
                                            WikiPage::author eq userLogin
                                        )?.let { page -> return@post call.respond(page) }
                                    }
                                }
                            }
                            Status.Ready -> {
                                pages.findOne(WikiPage::mainTitle eq title,
                                    WikiPage::status eq Status.Ready
                                )?.let { page -> return@post call.respond(page) }
                            }
                        }
                    }
                    call.respondText("The page from MongoDB not found!", status = HttpStatusCode.NotFound)
                }
                post("search") {
                    val searchText = call.receive<String>()
                    val result: List<String> = pages.find(WikiPage::status eq Status.Ready,
                        WikiPage::mainTitle regex(searchText)).toList().map { it.mainTitle }

                    if(result.isNotEmpty()) call.respond(result)
                    else call.respondText("Results not found!", status = HttpStatusCode.NotFound)
                }
                delete("forUsers") {
                    val (title, userLogin) = call.receive<Pair<String, String>>()

                    pages.findOne(WikiPage::mainTitle eq title, WikiPage::author eq userLogin)?.let {
                        pages.deleteOne(WikiPage::mainTitle eq title, WikiPage::author eq userLogin)
                        call.respondText("The page from MongoDB was successfully deleted!",
                            status = HttpStatusCode.OK)
                    } ?: call.respondText("The page from MongoDB not found!", status = HttpStatusCode.BadRequest)
                }
            }
            authorization(rightAdmin) {
                delete {
                    val title = call.receive<String>()

                    pages.findOne(WikiPage::mainTitle eq title)?.let {
                        pages.deleteOne(WikiPage::mainTitle eq title)
                        call.respondText("The page from MongoDB was successfully deleted!",
                            status = HttpStatusCode.OK)
                    } ?: call.respondText("The page from MongoDB not found!", status = HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
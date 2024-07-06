package ru.project.application.auth

import access.AccRight
import access.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.util.*

class AuthorizationConfig(
    var getRights: (User) -> Array<AccRight> = { emptyArray() }
)

class Authorization(internal var config: AuthorizationConfig) {
    companion object : BaseApplicationPlugin<Application, AuthorizationConfig, Authorization> {
        override val key: AttributeKey<Authorization> = AttributeKey("AuthorizationHolder")
        override fun install(pipeline: Application, configure: AuthorizationConfig.() -> Unit) =
            Authorization(AuthorizationConfig().apply(configure))
    }
}

class RouteAuthorizationConfig {
    var allowedRight: () -> AccRight? = { null }
}

val RouteAuthorization: RouteScopedPlugin<RouteAuthorizationConfig> = createRouteScopedPlugin(
    "RouteAuthorization", ::RouteAuthorizationConfig) {
    val holderConfig = application.plugin(Authorization).config
    val allowedRight = pluginConfig.allowedRight
    val getRights = holderConfig.getRights

    on(AuthenticationChecked) {
        val principal = it.authentication.principal<UserPrincipal>()
            ?: throw PrincipalError
        if (getRights(principal.user).none { accRight -> accRight.name == allowedRight()!!.name })
            throw AccessDenied
    }
}

fun Route.authorization(
    right: AccRight,
    build: Route.() -> Unit
): Route {
    val name = right.name
    val authenticatedRoute = createChild(AuthorizationRouteSelector(name))
    authenticatedRoute.install(RouteAuthorization) {
        this.allowedRight = { right }
    }
    authenticatedRoute.build()
    return authenticatedRoute
}


class AuthorizationRouteSelector(private val name: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }

    override fun toString(): String = "(authorize $name )"
}
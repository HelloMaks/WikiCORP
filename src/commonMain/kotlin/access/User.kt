package access

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class User(val login: String, val password: String, val rights: Array<AccRight> = emptyArray())

val User.json
    get() = Json.encodeToString(this)
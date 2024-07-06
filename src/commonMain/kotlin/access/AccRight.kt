package access

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class AccRight(val name: String)

val AccRight.json
    get() = Json.encodeToString(this)
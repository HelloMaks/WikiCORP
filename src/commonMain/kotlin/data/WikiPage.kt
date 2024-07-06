package data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class Status { Draft, Rejected, InProgress, Check, Ready }

val Status.json
    get() = Json.encodeToString(this)

@Serializable
class WikiPage(
    val mainTitle: String,
    val contents: Array<ContentPage>,
    val status: Status,
    val author: String,
    val reason: String = ""
)

val WikiPage.json
    get() = Json.encodeToString(this)

@Serializable
class TextInPage(val text: String, val height: Int)

@Serializable
class TitleInPage(val title: String)

@Serializable
class ImageInPage(val src: String, val sizes: Array<Double>, val sign: String)

@Serializable
class TableInPage(val values: InnerTable)

@Serializable
class ContentPage(
    val text: TextInPage? = null, val title: TitleInPage? = null,
    val image: ImageInPage? = null, val table: TableInPage? = null
)

@Serializable
data class InnerTable(val table: List<List<String>>)
package components

import csstype.*
import data.*
import emotion.react.css
import js.core.get
import kotlinx.serialization.Serializable
import react.*
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import userInfoContext
import web.dom.document
import web.file.FileReader
import web.html.HTMLImageElement
import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement
import web.html.InputType

/* Функция преобразования страницы в массив контентов */
fun makeContents(elemId: Int, innerTables: List<InnerTable>): Array<ContentPage> {
    var tableId = -1
    val contents: MutableList<ContentPage> = mutableListOf()
    repeat(elemId) { id ->
        arrayOf("textarea", "title", "img", "table").forEach { elemType ->
            document.getElementById("${elemType}_$id")?.let { elem ->
                when(elemType) {
                    "textarea" -> {
                        (elem as HTMLTextAreaElement).let { textArea ->
                            contents.add(
                                ContentPage(text = TextInPage(textArea.value, textArea.scrollHeight - 4))
                            )
                        }
                    }
                    "title" -> {
                        (elem as HTMLInputElement).let { titleInput ->
                            contents.add(ContentPage(title = TitleInPage(titleInput.value)))
                        }
                    }
                    "img" -> {
                        (elem as HTMLImageElement).let { img ->
                            document.getElementById("img_input_$id")?.let {
                                (it as HTMLInputElement).let { signInput ->
                                    contents.add(
                                        ContentPage(image = ImageInPage(img.src,
                                            arrayOf(img.width, img.height), signInput.value)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    "table" -> {
                        tableId += 1
                        contents.add(ContentPage(table = TableInPage(innerTables[tableId])))
                    }
                }
            }
        }
    }
    return contents.toTypedArray()
}

/* Функция преобразования массива контентов в элементы страницы */
fun makePage(contents: Array<ContentPage>): List<PageChild> {
    var tableId = -1
    val children: MutableList<PageChild> = mutableListOf()

    contents.forEachIndexed { idx, content ->
        content.text?.let { children.add(PageChild(child = CTextArea.create {
            this.id = idx; this.text = it.text; this.height = it.height
        })) }
        content.title?.let { children.add(PageChild(child = CTitle.create { this.id = idx; this.title = it.title })) }
        content.image?.let { children.add(PageChild(child = CImage.create {
            this.id = idx; this.src = it.src; this.sign = it.sign; this.sizes = it.sizes
        })) }
        content.table?.let { tableId += 1; children.add(PageChild(ids = arrayOf(idx, tableId))) }
    }
    return children
}

external interface AddPagesProps : Props {
    var mutation: (WikiPage) -> Unit
    var page: WikiPage?
}

@Serializable
class PageChild(val child: ReactNode? = null, val ids: Array<Int>? = null) // Класс дочерних элементов

val CAddPages = FC<AddPagesProps> { props ->
    var error by useState(false) // Регистрация ошибки ввода заголовка
    var addTable by useState(false) // Регистрация события создания таблиц
    var titlePage by useState(props.page?.mainTitle ?: "") // Главный заголовок страницы
    var innerTables: List<InnerTable> by useState(
        props.page?.let { page ->
            page.contents.filter { it.table != null }.map { it.table!!.values }
        } ?: emptyList()
    ) // Содержимое всех таблиц
    var ids by useState(
        props.page?.let { page ->
            arrayOf(page.contents.size, innerTables.size)
        } ?: arrayOf(1, 0)
    ) // ID дочерних элементов и таблиц
    var childList: List<PageChild> by useState(
        props.page?.let { page ->
            makePage(page.contents)
        } ?: listOf(PageChild(child = CTextArea.create { this.id = 0 }))
    ) // Коллекция дочерних компонентов

    val user = useContext(userInfoContext)!!.first // Текущий пользователь

    if(addTable) {
        childList = childList + PageChild(ids = ids)
        ids = arrayOf(ids[0] + 1, ids[1] + 1)
        addTable = false
    }

    div {
        div {
            input {
                type = InputType.text
                placeholder = "Введите заголовок страницы..."
                value = titlePage; onChange = { if(props.page == null) titlePage = it.target.value }
                css {
                    height = 5.vh; width = 100.pct; textAlign = TextAlign.center
                    fontSize = 36.px; fontWeight = FontWeight.bold
                    overflow = Overflow.hidden; border = 0.px; outline = 0.px
                    borderBottom = if(!error) Border(2.px, LineStyle.solid)
                    else Border(2.px, LineStyle.solid, Color("#ff0000"))
                    placeholder {
                        fontStyle = FontStyle.italic
                        if(error) color = Color("#cd5c5c")
                    }
                }
            }
            css {
                display = Display.flex; height = 8.vh
                alignItems = AlignItems.center; justifyContent = JustifyContent.center
            }
        }
        div {
            childList.forEach { _child ->
                _child.child?.let { reactNode -> child(reactNode) }
                _child.ids?.let { elemIds ->
                    val childTable = CTable.create {
                        this.id = elemIds[0]
                        this.innerTable = innerTables[elemIds[1]]
                        this.updateTable = { newTable ->
                            innerTables = innerTables.mapIndexed { idx, table ->
                                if(idx == elemIds[1]) newTable else table
                            }
                        }
                    }
                    child(childTable)
                }
            }
        }
        div {
            arrayOf("📃", "TITLE", "🖼", "#").forEachIndexed { idx, action ->
                if(idx != 2) {
                    button {
                        b { + action; css { fontSize = 16.px } }
                        when(idx) {
                            0 -> title = "Добавить текст"
                            1 -> title = "Добавить заголовок"
                            3 -> title = "Добавить таблицу"
                        }
                        onClick = {
                            when(idx) {
                                0 -> {
                                    childList = childList + PageChild(child = CTextArea.create { this.id = ids[0] })
                                }
                                1 -> { childList = childList + PageChild(child = CTitle.create { this.id = ids[0] }) }
                                3 -> {
                                    innerTables = innerTables + InnerTable(List(2) { List(2) { "" } })
                                    addTable = true
                                }
                            }
                            if(idx != 3) ids = arrayOf(ids[0] + 1, ids[1])
                        }
                        css {
                            height = 4.vh; width = 6.pct
                            if(idx != 0) marginLeft = 0.25.pct
                            textAlign = TextAlign.center
                            backgroundColor = Color("#FFFFFF")
                            border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                            hover { backgroundColor = Color("#777777") }
                            active { backgroundColor = Color("#444444") }
                        }
                    }
                }
                else {
                    label {
                        + action
                        title = "Добавить изображение"
                        input {
                            type = InputType.file
                            accept = "image/png, image/jpeg"
                            onChange = {
                                FileReader().let { fileReader ->
                                    fileReader.onload = {
                                        childList = childList + PageChild(child = CImage.create {
                                            this.id = ids[0]
                                            this.src = fileReader.result.toString()
                                        })
                                        ids = arrayOf(ids[0] + 1, ids[1])
                                    }
                                    fileReader.readAsDataURL(it.target.files!![0])
                                }
                            }
                            css { visibility = Visibility.hidden }
                        }
                        css {
                            height = 3.6.vh; width = 6.pct
                            fontSize = 24.px; if(idx != 0) marginLeft = 0.25.pct
                            textAlign = TextAlign.center
                            backgroundColor = Color("#FFFFFF")
                            border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                            hover { backgroundColor = Color("#777777") }
                            active { backgroundColor = Color("#444444") }
                        }
                    }
                }
            }
            css {
                display = Display.flex; marginBottom = 0.25.vh
                alignItems = AlignItems.center; justifyContent = JustifyContent.center
            }
        }
        div {
            arrayOf("Отправить на проверку", "Внести в черновик").forEachIndexed { idx, action ->
                button {
                    b { + action; css { fontSize = 16.px } }
                    onClick = {
                        if(titlePage.trim { it <= ' ' }.isNotEmpty()) {
                            if(error) error = false
                            if(idx == 0) {
                                props.mutation(
                                    WikiPage(titlePage, makeContents(ids[0], innerTables),
                                        Status.Check, user.login)
                                )
                            }
                            else {
                                props.mutation(
                                    WikiPage(titlePage, makeContents(ids[0], innerTables),
                                        Status.Draft, user.login)
                                )
                            }
                        }
                        else if(titlePage.trim { it <= ' ' }.isEmpty() && !error) error = true
                    }
                    css {
                        height = 4.vh; width = 12.375.pct
                        backgroundColor = Color("#FFFFFF"); textAlign = TextAlign.center
                        border = Border(2.px, LineStyle.solid); borderRadius = 5.px
                        hover { backgroundColor = Color("#777777") }
                        active { backgroundColor = Color("#444444") }
                        if(idx == 0) marginRight = 0.125.pct
                        if(idx == 1) marginLeft = 0.125.pct
                    }
                }
            }
            css {
                display = Display.flex
                alignItems = AlignItems.center; justifyContent = JustifyContent.center
            }
        }
    }
}
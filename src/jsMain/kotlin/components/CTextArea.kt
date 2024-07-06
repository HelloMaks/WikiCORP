package components

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.textarea

external interface TextAreaProps : Props {
    var id: Int?
    var text: String?
    var height: Int?
    var reason: String?
    var updReason: ((String) -> Unit)?
}

val CTextArea = FC<TextAreaProps> { props ->
    textarea {
        props.id?.let { id = "textarea_$it" }
        props.text?.let { defaultValue = it }
        props.reason?.let { if(it != "") value = "Причина отказа: $it" }
        placeholder = props.updReason?.let {
            "Напишите причину отказа, затем нажмите ещё раз на кнопку отказа..."
        } ?: "Введите текст..."
        onLoad = { event -> props.height?.let { event.currentTarget.style.height = "${it}px" } }
        onChange = { event ->
            event.currentTarget.let { elem ->
                elem.style.height = "auto"
                elem.style.height = "${elem.scrollHeight - 4}px"
            }
            props.updReason?.let { func -> func(event.target.value.substringAfter("Причина отказа: ")) }
        }
        css {
            resize = "none".unsafeCast<Resize>(); width = 100.pct
            fontFamily = FontFamily.sansSerif; fontSize = 28.px; fontWeight = FontWeight.bold
            overflow = Overflow.hidden; outline = 0.px
            props.reason?.let {
                border = Border(2.px, LineStyle.solid); borderRadius = 5.px
            } ?: run { border = 0.px }
            placeholder { fontStyle = FontStyle.italic }
        }
    }
}
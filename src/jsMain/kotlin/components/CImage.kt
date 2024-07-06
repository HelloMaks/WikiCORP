package components

import csstype.*
import emotion.react.css
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.html.HTMLImageElement
import web.html.InputType

external interface ImageProps : Props {
    var id: Int
    var src: String
    var sign: String?
    var sizes: Array<Double>?
}

val CImage = FC<ImageProps> { props ->
    var imgSign by useState(props.sign ?: "")
    var imgSize by useState(arrayOf(-1.0, -1.0))
    var imgScale by useState(arrayOf(1.0, 1.0))

    val imgRef = useRef<HTMLImageElement>()

    useEffect(imgSize) {
        props.sizes?.let { imgScale = arrayOf(it[0] / imgSize[0], it[1] / imgSize[1]) }
    }

    div {
        img {
            id = "img_${props.id}"; src = props.src; ref = imgRef
            onLoad = { imgSize = arrayOf(imgRef.current!!.width, imgRef.current!!.height) }
            height = imgSize[1] * imgScale[1]; width = imgSize[0] * imgScale[0]
            css { maxWidth = 100.pct }
        }
        input {
            id = "img_input_${props.id}"
            value = imgSign; placeholder = "Добавьте описание..."
            onChange = { imgSign = it.target.value }
            css {
                textAlign = TextAlign.center
                fontSize = 24.px; fontWeight = FontWeight.bold
                width = 100.pct; border = 0.px; outline = 0.px
            }
        }
        label {
            + "Масштаб по высоте: ${imgScale[1]}"
            input {
                type = InputType.range; step = 0.01
                min = 0.01; max = 1.0; value = imgScale[1]
                onChange = {
                    imgScale = arrayOf(imgScale[0], it.target.value.toDouble())
                }
            }
            css { fontSize = 24.px; fontWeight = FontWeight.bold }
        }
        label {
            + "Масштаб по ширине: ${imgScale[0]}"
            input {
                type = InputType.range; step = 0.01
                min = 0.01; max = 1.0; value = imgScale[0]
                onChange = { imgScale = arrayOf(it.target.value.toDouble(), imgScale[1]) }
            }
            css { fontSize = 24.px; fontWeight = FontWeight.bold }
        }
        css {
            display = Display.flex; flexDirection = FlexDirection.column
            alignItems = AlignItems.center; justifyContent = JustifyContent.center
        }
    }
}
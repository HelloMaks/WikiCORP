package components

import CMainTitle
import csstype.WhiteSpace
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.div

val CMainPage = FC<Props> {
    val welcomeText = "Добро Пожаловать в CORP`s Wiki, энциклопедию для решения различных задач " +
            "по работе с информацией, используемой в рамках корпорации. Обратите внимание на возможности, " +
            "предоставляемые кнопками из верхнего и бокового меню:\n[[вставьте текст]]"

    CMainTitle { this.title = "Заглавная страница" }
    div { b { + welcomeText; css { fontSize = 28.px; whiteSpace = WhiteSpace.preWrap } } }
}
package data

import access.User
import access.AccRight

val rightUser = AccRight("rightUser")
val rightAdmin = AccRight("rightAdmin")

val user = User("user", "123", arrayOf(rightUser))
val admin = User("admin","123", arrayOf(rightUser, rightAdmin))
val userList = listOf(user, admin)
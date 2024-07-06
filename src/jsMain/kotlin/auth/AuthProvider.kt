package auth

import UserInfo
import react.*
import userInfoContext

fun ChildrenBuilder.authProvider(block: ChildrenBuilder.() -> Unit) =
    child(
        FC<PropsWithChildren> {
            var userInfo by useState<UserInfo>(null) // Информация об аутентифицируемом пользователе
            CAuthContainer {
                user = userInfo?.first
                signIn = { userInfo = it }
                signOff = { userInfo = null }
            }
            if(userInfo != null)
                userInfoContext.Provider(userInfo) { block() }
        }.create()
    )
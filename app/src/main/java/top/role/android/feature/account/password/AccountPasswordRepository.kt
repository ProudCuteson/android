package top.role.android.feature.account.password

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.account.MobilePasswordResult


class AccountPasswordRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    suspend fun setupPassword(
        password:
        String,
    ): Result<MobilePasswordResult> {

        return convexClient.action(
            name =
                "mobile/native/account/password:setupPhonePasswordMobile",

            args =
                mapOf(
                    "password" to
                            password,
                ),
        )
    }


    suspend fun changePassword(
        currentPassword:
        String,

        nextPassword:
        String,
    ): Result<MobilePasswordResult> {

        return convexClient.action(
            name =
                "mobile/native/account/password:changePhonePasswordMobile",

            args =
                mapOf(
                    "currentPassword" to
                            currentPassword,

                    "nextPassword" to
                            nextPassword,
                ),
        )
    }
}
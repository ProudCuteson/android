package top.role.android.feature.account

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.account.MobileAccountHome


class AccountRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeAccountHome():
            Flow<Result<MobileAccountHome>> {

        return convexClient.subscribe(
            name =
                "mobile/native/account/get:getMobileAccountHome",

            args =
                emptyMap(),
        )
    }
}
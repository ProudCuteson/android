package top.role.android.feature.market.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.core.payment.WechatPayLauncher


class MarketDetailViewModelFactory(
    private val listingId:
    String,

    private val repository:
    MarketDetailRepository,

    private val wechatPayLauncher:
    WechatPayLauncher,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                MarketDetailViewModel::class.java
            )
        ) {
            return MarketDetailViewModel(
                listingId =
                    listingId,

                repository =
                    repository,

                wechatPayLauncher =
                    wechatPayLauncher,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
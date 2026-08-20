package top.role.android.feature.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.core.model.market.MobileMarketListMode


class MarketListViewModelFactory(
    private val mode:
    MobileMarketListMode,

    private val repository:
    MarketListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        return MarketListViewModel(
            mode =
                mode,

            repository =
                repository,
        ) as T
    }
}
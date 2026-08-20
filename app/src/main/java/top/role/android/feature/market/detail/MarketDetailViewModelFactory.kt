package top.role.android.feature.market.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class MarketDetailViewModelFactory(
    private val listingId:
    String,

    private val repository:
    MarketDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        return MarketDetailViewModel(
            listingId =
                listingId,

            repository =
                repository,
        ) as T
    }
}
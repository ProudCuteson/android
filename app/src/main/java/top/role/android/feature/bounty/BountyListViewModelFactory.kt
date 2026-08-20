package top.role.android.feature.bounty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.core.model.bounty.MobileBountyListMode


class BountyListViewModelFactory(
    private val mode:
    MobileBountyListMode,

    private val repository:
    BountyListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        return BountyListViewModel(
            mode =
                mode,

            repository =
                repository,
        ) as T
    }
}
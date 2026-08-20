package top.role.android.feature.bounty.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BountyDetailViewModelFactory(
    private val bountyId:
    String,

    private val repository:
    BountyDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {
        return BountyDetailViewModel(
            bountyId =
                bountyId,

            repository =
                repository,
        ) as T
    }
}
package za.co.woolworths.financial.services.android.presentation.addtolist.components

import za.co.woolworths.financial.services.android.models.dto.ShoppingList

data class AddToListUiState(
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val isAddToListInProgress: Boolean = false,
    val isAddToListSuccess: Boolean = false,
    val showCreateList: Boolean = false,
    val list: List<ShoppingList> = emptyList(),
    val selectedListItem: List<ShoppingList> = emptyList()
)
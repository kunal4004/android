package za.co.woolworths.financial.services.android.presentation.addtolist.components

import za.co.woolworths.financial.services.android.models.dto.ShoppingList

sealed class AddToListScreenEvents {

    object CreateListClick : AddToListScreenEvents()
    object RetryClick : AddToListScreenEvents()
    object ConfirmClick: AddToListScreenEvents()
    object CancelClick: AddToListScreenEvents()
    object CreateListBackPressed: AddToListScreenEvents()

    data class OnItemClick(val item: ShoppingList) : AddToListScreenEvents()
    data class ConfirmCreateList(val name: String) : AddToListScreenEvents()

}

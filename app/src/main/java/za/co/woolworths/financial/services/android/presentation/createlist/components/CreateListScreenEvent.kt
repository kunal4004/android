package za.co.woolworths.financial.services.android.presentation.createlist.components

sealed class CreateListScreenEvent {

    object BackPressed: CreateListScreenEvent()
    object CancelClick: CreateListScreenEvent()

    data class CreateList(val listName: String): CreateListScreenEvent()
}
package za.co.woolworths.financial.services.android.presentation.addtolist.components

data class CreateNewListState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val title: String = "",
    val cancelText: String = ""
)

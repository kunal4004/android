package za.co.woolworths.financial.services.android.presentation.addtolist.components

import com.awfs.coordination.R

data class CreateNewListState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val errorMessageId: Int = R.string.oops_error_message,
    val title: String = "",
    val cancelText: String = ""
)

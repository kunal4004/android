package za.co.woolworths.financial.services.android.viewmodels

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.ConfirmationDialogEvents
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.ConfirmationDialogUiState
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.DeleteListConfirmationUiState
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.DeleteProgressViewUiState
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_DELETE_LIST_CONFIRMED
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SCREEN_NAME_DELETE_LIST_CONFIRMATION
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SCREEN_NAME_DELETE_LIST_PROGRESS_BAR
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_DONT_ASK_AGAIN_CHECKED
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_ITEM
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_POSITION
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_SCREEN_NAME
import javax.inject.Inject

@HiltViewModel
class ConfirmationViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var _uiState: MutableStateFlow<ConfirmationDialogUiState> =
        MutableStateFlow(ConfirmationDialogUiState.None)
    val uiState: StateFlow<ConfirmationDialogUiState> = _uiState.asStateFlow()

    // Progress UI state
    val deleteProgressViewUiState = mutableStateOf(DeleteProgressViewUiState())

    // Delete List UI state
    val deleteListConfirmationUiState = mutableStateOf(DeleteListConfirmationUiState())

    init {
        val screenName = savedStateHandle.get<String>(BUNDLE_KEY_SCREEN_NAME) ?: ""
        setScreenUi(screenName)
    }

    private fun setScreenUi(screenName: String) {
        viewModelScope.launch {
            _uiState.emit(
                when (screenName) {
                    SCREEN_NAME_DELETE_LIST_CONFIRMATION -> {
                        deleteListConfirmationUiState.value =
                            deleteListConfirmationUiState.value.copy(
                                showCheckBox = true
                            )
                        ConfirmationDialogUiState.StateDeleteListConfirmation
                    }

                    SCREEN_NAME_DELETE_LIST_PROGRESS_BAR -> {
                        val item = savedStateHandle.get<ShoppingList>(BUNDLE_KEY_ITEM)
                        item?.let {
                            deleteProgressViewUiState.value = deleteProgressViewUiState.value.copy(
                                title = R.string.my_list_deleting_title,
                                desc = R.string.processing_your_request_desc,
                                listName = it.listName
                            )
                        } ?: throw(IllegalStateException("Delete List item not found."))

                        ConfirmationDialogUiState.StateDeleteProgress
                    }

                    else -> ConfirmationDialogUiState.None
                }
            )
        }
    }

    fun onEvent(event: ConfirmationDialogEvents) {
        when (event) {
            is ConfirmationDialogEvents.OnCheckedChange -> {
                deleteListConfirmationUiState.value = deleteListConfirmationUiState.value.copy(
                    isCheckedDoNotAskAgain = event.isChecked
                )
            }

            ConfirmationDialogEvents.OnConfirmClick -> {}

            ConfirmationDialogEvents.OnDismissClick -> {}
        }
    }

    fun getBundleData(): Bundle {
        val bundleKey = savedStateHandle.get<String>(BUNDLE_KEY)
        val bundle = bundleOf(
            BUNDLE_KEY to bundleKey
        ).also {
            when (bundleKey) {
                RESULT_DELETE_LIST_CONFIRMED -> {
                    it.putParcelable(
                        BUNDLE_KEY_ITEM,
                        savedStateHandle.get<ShoppingList>(BUNDLE_KEY_ITEM)
                    )
                    it.putInt(
                        BUNDLE_KEY_POSITION,
                        savedStateHandle.get<Int>(BUNDLE_KEY_POSITION) ?: -1
                    )
                    it.putBoolean(
                        BUNDLE_KEY_DONT_ASK_AGAIN_CHECKED,
                        deleteListConfirmationUiState.value.isCheckedDoNotAskAgain
                    )
                }

                else -> {}
            }
        }
        return bundle
    }

    fun getBundleKey() = savedStateHandle.get<String>(BUNDLE_KEY)
}
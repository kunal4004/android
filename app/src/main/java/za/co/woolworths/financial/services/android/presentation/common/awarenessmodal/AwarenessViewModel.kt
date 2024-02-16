package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.security.InvalidKeyException
import javax.inject.Inject

@HiltViewModel
class AwarenessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    fun isNoteChecked(): Boolean = screenState.value.isChecked
    fun setNoteChecked(value: Boolean){
        _screenState.update { it.copy(isChecked = value) }
    }

    private var _screenState = MutableStateFlow(AwarenessModalState())
    val screenState = _screenState.asStateFlow()

    init {
        val modalName = savedStateHandle.get<AwarenessModalNames>(AppConstant.MODAL_NAME)
        when(modalName) {
            AwarenessModalNames.SUBSTITUTIONS -> {
                _screenState.update {
                    it.copy(
                        iconAwareness = R.drawable.ic_awareness_substitute,
                        awarenessTitle = R.string.awareness_substitute_title,
                        awarenessDesc = R.string.awareness_substitute_desc,
                        confirmButton = R.string.choose_substitutes,
                        dismissButton = R.string.continue_to_checkout,
                        noteText = R.string.my_list_delete_this_list_checkbox_title
                    )
                }
            }
            else -> {
                FirebaseManager.logException(InvalidKeyException("Modal name is not present"))
            }
        }
    }
}


enum class AwarenessModalNames {
    SUBSTITUTIONS
}
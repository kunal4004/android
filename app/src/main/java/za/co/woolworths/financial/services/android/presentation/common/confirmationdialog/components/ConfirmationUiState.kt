package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components

import android.os.Parcelable
import androidx.annotation.StringRes
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfirmationUiState(
    @StringRes val title: Int = R.string.my_list_delete_this_list,
    @StringRes val desc: Int = R.string.my_list_delete_this_list_desc,
    @StringRes val checkBoxTitle: Int = R.string.my_list_delete_this_list_checkbox_title,
    @StringRes val confirmText: Int = R.string.remove,
    @StringRes val cancelText: Int = R.string.cancel,
    val showCheckBox: Boolean = false,
    val isChecked: Boolean = false
) : Parcelable

package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components

import android.os.Parcelable
import androidx.annotation.StringRes
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeleteProgressViewUiState(
    @StringRes val title: Int = R.string.processing_your_request,
    @StringRes val desc: Int = R.string.processing_your_request_desc,
    val listName: String = ""
) : Parcelable

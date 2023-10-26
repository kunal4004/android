package za.co.woolworths.financial.services.android.shoppinglist.component

import android.os.Parcelable
import androidx.annotation.StringRes
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppbarUiState(
    @StringRes val titleRes: Int = R.string.my_shopping_lists,
    @StringRes val rightButtonRes: Int = R.string.edit,
    val showRightButton: Boolean = false
) : Parcelable

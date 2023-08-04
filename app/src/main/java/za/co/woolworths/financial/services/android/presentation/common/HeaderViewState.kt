package za.co.woolworths.financial.services.android.presentation.common

import androidx.annotation.DrawableRes
import com.awfs.coordination.R

sealed class HeaderViewState {

    data class HeaderStateType1(
        @DrawableRes val icon: Int = R.drawable.back24,
        val title: String = ""
    ) : HeaderViewState()

    data class HeaderStateType2(
        @DrawableRes val icon: Int = R.drawable.add_black,
        val title: String = ""
    ) : HeaderViewState()
}
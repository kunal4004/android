package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2

class OffsetPageTransformer(
    @Px private val offsetPx: Int,
    @Px private val pageMarginPx: Int
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {

        val offset = position * -(2 * offsetPx + pageMarginPx)
        val totalMargin = offsetPx + pageMarginPx

        page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = totalMargin
            marginEnd = totalMargin
            topMargin = 0
            bottomMargin = 0
        }

        page.translationX = offset
        page.translationZ = offset

    }
}

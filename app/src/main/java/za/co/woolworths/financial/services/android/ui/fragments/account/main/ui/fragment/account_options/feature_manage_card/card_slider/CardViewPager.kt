package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.disableNestedScrolling
import javax.inject.Inject

interface ICardViewPager {
    fun ViewPager2.onPageChangeListener(
        cardAdapter: ManageCardScreenSlidesAdapter,
        onPageSwipeListener: (StoreCard?) -> Unit
    )
}

class CardViewPager @Inject constructor() : ICardViewPager {

    companion object {
        private const val OFFSET = 100
    }

    inner class OffsetPageTransformer(
        @Px private val offsetPx: Int,
        @Px private val pageMarginPx: Int
    ) : ViewPager2.PageTransformer {

        override fun transformPage(page: View, position: Float) {
            val viewPager = requireViewPager(page)
            val offset = position * -(2 * offsetPx + pageMarginPx)
            val totalMargin = offsetPx + pageMarginPx

            if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = totalMargin
                    marginEnd = totalMargin
                }

                page.translationX =
                    if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        -offset
                    } else {
                        offset
                    }

                page.translationZ =
                    if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        -offset
                    } else {
                        offset
                    }

            } else {
                page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = totalMargin
                    bottomMargin = totalMargin
                }

                page.translationY = offset
                page.translationZ = offset
            }
        }

        private fun requireViewPager(page: View): ViewPager2 {
            val parent = page.parent
            val parentParent = parent.parent
            if (parent is RecyclerView && parentParent is ViewPager2) {
                return parentParent
            }
            throw IllegalStateException(
                "Expected the page view to be managed by a ViewPager2 instance."
            )
        }
    }

    operator fun invoke(
        viewPager: ViewPager2?,
        tab: TabLayout,
        cardAdapter: ManageCardScreenSlidesAdapter,
        onPageSwipeListener: (StoreCard?) -> Unit
    ) {
        viewPager?.apply {
            disableNestedScrolling()
            offscreenPageLimit = 3
            setPageTransformer(OffsetPageTransformer(OFFSET, OFFSET))
            adapter = cardAdapter
            TabLayoutMediator(tab, this) { _, _ -> }.attach()

            onPageChangeListener(cardAdapter, onPageSwipeListener)
        }
    }

    override fun ViewPager2.onPageChangeListener(
        cardAdapter: ManageCardScreenSlidesAdapter,
        onPageSwipeListener: (StoreCard?) -> Unit
    ) {
        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val listOfPrimaryStoreCards = cardAdapter.getListOfStoreCards()
                if (listOfPrimaryStoreCards?.size ?: 0 > 0) {
                    val storeCard = listOfPrimaryStoreCards?.get(position)
                    onPageSwipeListener(storeCard)
                }
            }
        })
    }

}
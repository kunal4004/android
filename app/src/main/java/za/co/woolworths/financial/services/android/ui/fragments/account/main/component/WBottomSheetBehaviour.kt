package za.co.woolworths.financial.services.android.ui.fragments.account.main.component

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.annotation.DimenRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.ui.extension.deviceHeight
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface IBottomSheetBehaviour {
    val deviceHeight: Int
    val bottomGuidelinePercent: Float
    val isProductGoodStanding: Boolean
    val buttonsTopGuideline: Float
    val buttonsBottomGuideline: Float
    fun init(bottomSheetBehaviourView: ConstraintLayout?): BottomSheetBehavior<*>
    fun peekHeight(): Int
    fun expandedHeight(): Int
    fun callback(offset: (Float) -> Unit): BottomSheetBehavior.BottomSheetCallback
    fun animateDim(slideOffset: Float, view: View?)
    fun statusBarHeight(): Int
}

class WBottomSheetBehaviour @Inject constructor(
    @ApplicationContext val context: Context,
    private val screenState: AccountProductLandingDao
) : IBottomSheetBehaviour, IAccountProductLandingDao by screenState {

    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    val statusBarHeight = statusBarHeight()

    override val deviceHeight: Int
        get() = deviceHeight()

    override val isProductGoodStanding: Boolean
        get() = screenState.isProductInGoodStanding()

    override val buttonsTopGuideline: Float
        get() = getDimension(if (isProductGoodStanding) R.dimen.account_good_standing_button_start_guideline else R.dimen.account_not_good_standing_button_start_guideline)

    override val buttonsBottomGuideline: Float
        get() = getDimension(if (isProductGoodStanding) R.dimen.account_good_standing_button_end_guideline else R.dimen.account_not_good_standing_end_guideline)

    override val bottomGuidelinePercent: Float
        get() = getDimension(if (isProductGoodStanding) R.dimen.peek_height_good_standing else R.dimen.peek_height_not_good_standing)

    override fun init(bottomSheetBehaviourView: ConstraintLayout?): BottomSheetBehavior<*> {
        bottomSheetBehaviourView?.apply {
            layoutParams?.height = expandedHeight()
            requestLayout()
            sheetBehavior = BottomSheetBehavior.from(this)
            sheetBehavior.peekHeight = peekHeight()
        }
        return sheetBehavior
    }

    override fun peekHeight(): Int = deviceHeight - (deviceHeight * bottomGuidelinePercent).toInt()

    override fun expandedHeight() =
        deviceHeight - if (Build.VERSION.SDK_INT >= 30) statusBarHeight * 6 else statusBarHeight

    override fun statusBarHeight(): Int =
        context.resources?.getDimensionPixelSize(R.dimen._4sdp) ?: 0

    override fun callback(offset: (Float) -> Unit): BottomSheetBehavior.BottomSheetCallback {
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                offset(slideOffset)
            }
        }
    }

    override fun animateDim(slideOffset: Float, view: View?) {
        val colorFrom = ContextCompat.getColor(context, android.R.color.transparent)
        val colorTo = ContextCompat.getColor(context, R.color.black_99)
        view?.setBackgroundColor(KotlinUtils.interpolateColor(slideOffset, colorFrom, colorTo))
    }

    private fun getDimension(@DimenRes dimensId: Int): Float {
        val typedValue = TypedValue()
        context.resources?.getValue(dimensId, typedValue, true)
        return typedValue.float
    }

}
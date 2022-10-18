package za.co.woolworths.financial.services.android.ui.fragments.account.applynow

import android.util.TypedValue
import android.view.View
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.extension.deviceHeight
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface IApplyNowBottomSheetImpl{
    var sheetBehavior: BottomSheetBehavior<*>?
    fun setupBottomSheetBehaviour(bottomSheet:View)
}
class ApplyNowBottomSheetImpl@Inject constructor() :IApplyNowBottomSheetImpl{
    override var sheetBehavior: BottomSheetBehavior<*>? = null

    override fun setupBottomSheetBehaviour(bottomSheet:View) {
        val bottomSheetBehaviourLinearLayout = bottomSheet
        val layoutParams = bottomSheetBehaviourLinearLayout.layoutParams
        layoutParams?.height = bottomSheetBehaviourHeight()
        bottomSheetBehaviourLinearLayout.requestLayout()
        sheetBehavior = BottomSheetBehavior.from(bottomSheetBehaviourLinearLayout)
        sheetBehavior?.peekHeight = bottomSheetPeekHeight()

    }
    fun bottomSheetBehaviourHeight(): Int {
        val height = deviceHeight()
        val toolbarHeight = KotlinUtils.getToolbarHeight()
        return height.minus(toolbarHeight).minus(KotlinUtils.getStatusBarHeight().div(2))
    }
    fun bottomSheetPeekHeight(): Int {
        val deviceHeight = deviceHeight()
        val bottomGuidelineTypeValue = TypedValue()
        WoolworthsApplication.getInstance()?.resources?.getValue(
            R.integer.my_account_apply_now_bottom_sheet_slider_peek_height,
            bottomGuidelineTypeValue,
            true
        )
        return (deviceHeight - (deviceHeight * bottomGuidelineTypeValue.float)).toInt()
    }
}
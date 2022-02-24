package za.co.woolworths.financial.services.android.ui.fragments.account.main.component

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.ui.extension.deviceHeight
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface IBottomSheetBehaviour {
    fun init(bottomSheetBehaviourView: ConstraintLayout?): BottomSheetBehavior<*>
    fun heightOfDevice(): Int
    fun peekHeight(): Int
    fun expandedHeight(): Int
    fun callback(
        offset: (Float) -> Unit
    ): BottomSheetBehavior.BottomSheetCallback

    fun animateDim(slideOffset: Float, view: View?)
}

class WBottomSheetBehaviour @Inject constructor(@ApplicationContext val context: Context) :
    IBottomSheetBehaviour {

    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun heightOfDevice(): Int = deviceHeight()

    override fun peekHeight(): Int = heightOfDevice() / 3

    override fun expandedHeight() = heightOfDevice()

    override fun init(bottomSheetBehaviourView: ConstraintLayout?): BottomSheetBehavior<*> {
        bottomSheetBehaviourView?.apply {
            layoutParams?.height = expandedHeight()
            requestLayout()
            sheetBehavior = BottomSheetBehavior.from(this)
            sheetBehavior.peekHeight = peekHeight()
        }
        return sheetBehavior
    }

    override fun callback(
        offset: (Float) -> Unit
    ): BottomSheetBehavior.BottomSheetCallback {
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
}
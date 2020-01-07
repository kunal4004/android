package za.co.woolworths.financial.services.android.ui.views

import android.view.View
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback

class BottomSheetDialog(private val layoutBottomSheet: LinearLayout?, private val anchoredHeight: Int, private val listener: (View, Int) -> Unit) {

    private var sheetBehavior: BottomSheetBehavior<*>? = null

    fun show() {
        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(layoutBottomSheet)
        sheetBehavior?.peekHeight = anchoredHeight
        sheetBehavior?.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                listener.invoke(bottomSheet, newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }
}
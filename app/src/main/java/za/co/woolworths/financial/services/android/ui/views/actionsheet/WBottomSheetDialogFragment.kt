package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class WBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = context?.let { BottomSheetDialog(it, theme) }!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.apply {
            setOnShowListener { dialog ->
                val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout?
                BottomSheetBehavior.from(bottomSheet)?.state = BottomSheetBehavior.STATE_EXPANDED
            }

        }
    }
}
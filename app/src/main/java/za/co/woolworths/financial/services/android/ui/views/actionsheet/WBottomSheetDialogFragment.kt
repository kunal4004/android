package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.crashlytics.android.Crashlytics
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class WBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var bottomSheet: FrameLayout? = null
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
                bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout?
                bottomSheet?.let {  sheet ->  BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED}
            }

        }
    }

    // Override dialog.show() method to prevent IllegalStateException being thrown
    // when running a dialog fragment on a destroyed activity.
    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (ex: IllegalStateException) {
            Crashlytics.logException(ex)
        }
    }
}
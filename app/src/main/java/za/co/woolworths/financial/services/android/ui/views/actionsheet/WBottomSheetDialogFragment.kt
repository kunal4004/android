package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.FirebaseManager
import java.lang.Exception

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
            if (!manager.isDestroyed && !manager.isStateSaved) {
                super.show(manager, tag)
            }
        }catch(ex: Exception) {
            FirebaseManager.logException(ex)
        }
    }
}
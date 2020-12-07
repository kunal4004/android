package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_dialog_fragment.*
import kotlinx.android.synthetic.main.root_device_info_fragment.tvDescription
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMADeleteCardAPIErrorFragment : WBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.error_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvDescription?.text = bindString(R.string.pma_delete_card_failure_textview)

        okButtonTapped?.setOnClickListener {
            AnimationUtilExtension.animateViewPushDown(it)
            dismiss()
        }
    }
}
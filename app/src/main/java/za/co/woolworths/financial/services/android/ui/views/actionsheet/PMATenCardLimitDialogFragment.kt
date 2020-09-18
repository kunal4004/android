package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.root_device_info_fragment.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMATenCardLimitDialogFragment : WBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_ten_card_limit_exceed_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        okButtonTapped?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener { dismiss() }
        }
    }
}
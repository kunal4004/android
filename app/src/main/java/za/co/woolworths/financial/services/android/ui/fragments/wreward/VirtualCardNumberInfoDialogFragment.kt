package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.virtual_card_number_info_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class VirtualCardNumberInfoDialogFragment : WBottomSheetDialogFragment() {

    companion object {
        fun newInstance() = VirtualCardNumberInfoDialogFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.virtual_card_number_info_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gotItButton.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

}
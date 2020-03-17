package za.co.woolworths.financial.services.android.ui.fragments.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.gift_with_purchase_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class GiftWithPurchaseDialogDetailFragment : WBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.gift_with_purchase_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gotITButton?.setOnClickListener { dismiss() }

    }
}
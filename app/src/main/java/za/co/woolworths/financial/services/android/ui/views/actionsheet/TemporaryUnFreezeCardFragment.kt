package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temporary_freeze_cart_layout.cancelTextView
import kotlinx.android.synthetic.main.temporary_unfreeze_cart_layout.*
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze

class TemporaryUnFreezeCardFragment(private val iTemporaryCardFreeze: ITemporaryCardFreeze?) : WBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.temporary_unfreeze_cart_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelTextView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        cancelTextView?.setOnClickListener {
            iTemporaryCardFreeze?.onTemporaryCardUnFreezeCanceled()
            dismiss()
        }

        unfreezeMyCardButton?.setOnClickListener {
            iTemporaryCardFreeze?.onTemporaryCardUnFreezeConfirmed()
            dismiss()
        }
    }
}
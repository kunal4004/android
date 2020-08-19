package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temporary_freeze_cart_layout.*
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class TemporaryFreezeCardFragment(private val iTemporaryCardFreeze: ITemporaryCardFreeze?) : WBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.temporary_freeze_cart_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelTextView?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                iTemporaryCardFreeze?.onTemporaryCardFreezeCanceled()
                dismiss()
            }
            AnimationUtilExtension.animateViewPushDown(this)
        }

        confirmFreezeCardButton?.apply {
            setOnClickListener {
                iTemporaryCardFreeze?.onTemporaryCardFreezeConfirmed()
                dismiss()
            }
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        iTemporaryCardFreeze?.onTemporaryCardFreezeCanceled()
    }
}
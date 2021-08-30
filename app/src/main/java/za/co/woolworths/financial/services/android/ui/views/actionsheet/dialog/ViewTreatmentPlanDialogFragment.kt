package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_treatment_plan_dialog_fragment.*
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ViewTreatmentPlanDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = ViewTreatmentPlanDialogFragment::class.java.simpleName

    companion object {
        const val VIEW_PAYMENT_PLAN_BUTTON = "viewPaymentPlanButton"
        const val MAKE_A_PAYMENT_BUTTON = "makeAPaymentButton"

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountSignedInActivity){
            showChatBubbleInterface = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_treatment_plan_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewTreatmentPlanButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        makePaymentButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        closeIconImageButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        makePaymentButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.viewTreatmentPlanButton -> {
                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to VIEW_PAYMENT_PLAN_BUTTON))
            }

            R.id.makePaymentButton -> {
                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to MAKE_A_PAYMENT_BUTTON))
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}
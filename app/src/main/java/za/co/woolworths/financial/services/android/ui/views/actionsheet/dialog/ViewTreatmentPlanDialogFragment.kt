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
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ViewTreatmentPlanDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = ViewTreatmentPlanDialogFragment::class.java.simpleName
    private lateinit var dialogButtonType: ViewTreatmentPlanDialogButtonType
    companion object {
        enum class ViewTreatmentPlanDialogButtonType { CC_ACTIVE, PL_ELIGIBLE, PL_SC_NORMAL }
        const val VIEW_PAYMENT_PLAN_BUTTON = "viewPaymentPlanButton"
        const val MAKE_A_PAYMENT_BUTTON = "makeAPaymentButton"
        const val PLAN_BUTTON_TYPE = "buttonType"

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

        dialogButtonType = arguments?.getSerializable(PLAN_BUTTON_TYPE) as ViewTreatmentPlanDialogButtonType

        mainButton?.apply {
            text = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ELIGIBLE)
                bindString(R.string.make_payment_now_button_label)
            else bindString(R.string.view_payment_plan_button_label)
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        closeIconImageButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        makePaymentButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_SC_NORMAL) View.VISIBLE else View.GONE
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        viewPaymentOptionsButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.CC_ACTIVE) View.VISIBLE else View.GONE
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        cannotAffordPaymentButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ELIGIBLE) View.VISIBLE else View.GONE
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        viewTreatmentPlanDescriptionTextView?.apply {
            text = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ELIGIBLE)
                bindString(R.string.treatment_plan_eligible_description_pl) else bindString(R.string.view_treatment_plan_description)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.mainButton -> {
                dismiss()
                if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ELIGIBLE)
                    setFragmentResult(mClassName, bundleOf(mClassName to MAKE_A_PAYMENT_BUTTON))
                else setFragmentResult(mClassName, bundleOf(mClassName to VIEW_PAYMENT_PLAN_BUTTON))
            }

            R.id.makePaymentButton, R.id.viewPaymentOptionsButton -> {
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
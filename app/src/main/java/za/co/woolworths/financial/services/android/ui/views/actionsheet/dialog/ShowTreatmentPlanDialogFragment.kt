package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ViewTreatmentPlanDialogFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseAppCompatDialogFragmentBinding

class ShowTreatmentPlanDialogFragment : BaseAppCompatDialogFragmentBinding<ViewTreatmentPlanDialogFragmentBinding>(ViewTreatmentPlanDialogFragmentBinding::inflate), View.OnClickListener {

    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = ViewTreatmentPlanDialogFragment::class.java.simpleName
    private var viewPaymentOption: Boolean = false
    private var state: String? = null

    companion object {
        const val VIEW_PAYMENT_PLAN_BUTTON = "viewPaymentPlanButton"
        const val MAKE_A_PAYMENT_BUTTON = "makeAPaymentButton"
        const val VIEW_PAYMENT_OPTIONS_VISIBILITY = "viewPaymentOptionsVisibility"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountSignedInActivity) {
            showChatBubbleInterface = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPaymentOption = arguments?.getBoolean(VIEW_PAYMENT_OPTIONS_VISIBILITY, false) ?: false
        state =
            arguments?.getString(ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE)

        binding.apply {
            mainButton?.apply {
                setOnClickListener(this@ShowTreatmentPlanDialogFragment)
                AnimationUtilExtension.animateViewPushDown(this)
            }

            makePaymentButton?.apply {
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                visibility = if (viewPaymentOption) View.VISIBLE else View.GONE
                setOnClickListener(this@ShowTreatmentPlanDialogFragment)
                AnimationUtilExtension.animateViewPushDown(this)
            }

            closeIconImageButton?.apply {
                setOnClickListener(this@ShowTreatmentPlanDialogFragment)
                AnimationUtilExtension.animateViewPushDown(this)
            }

            viewPaymentOptionsButton?.apply {
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                visibility = if (viewPaymentOption) View.GONE else View.VISIBLE
                setOnClickListener(this@ShowTreatmentPlanDialogFragment)
                AnimationUtilExtension.animateViewPushDown(this)
            }
            handlePopupForCC()
        }
    }

    override fun onClick(view: View?) {
        KotlinUtils.avoidDoubleClicks(view)
        when (view?.id) {

            R.id.mainButton -> {
                dismiss()
                setFragmentResult(
                    mClassName,
                    bundleOf(VIEW_PAYMENT_PLAN_BUTTON to VIEW_PAYMENT_PLAN_BUTTON)
                )
            }

            R.id.makePaymentButton, R.id.viewPaymentOptionsButton -> {
                dismiss()
                setFragmentResult(
                    mClassName,
                    bundleOf(MAKE_A_PAYMENT_BUTTON to MAKE_A_PAYMENT_BUTTON)
                )
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    private fun ViewTreatmentPlanDialogFragmentBinding.handlePopupForCC() {
        when (state) {
            ProductGroupCode.CC.value-> {
                viewTreatmentPlanDescriptionTextView.text = bindString(
                    R.string.treatment_plan_eligible_charged_off_description_pl
                )
                viewTreatmentPlanTitleTextView.text = bindString(
                    R.string.remove_block_on_collection_dialog_title
                )
                mainButton.text = bindString(R.string.view_payment_plan_button_label)
                makePaymentButton.visibility = View.GONE

            }
        }
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}
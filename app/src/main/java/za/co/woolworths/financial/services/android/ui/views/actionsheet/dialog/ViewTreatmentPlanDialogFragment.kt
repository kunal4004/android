package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_treatment_plan_dialog_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ViewTreatmentPlanDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = ViewTreatmentPlanDialogFragment::class.java.simpleName
    private var dialogButtonType: ViewTreatmentPlanDialogButtonType? = null
    private var state: ApplyNowState? = null
    private var eligibilityPlan: EligibilityPlan? = null

    companion object {
        enum class ViewTreatmentPlanDialogButtonType {
            CC_ACTIVE,
            PL_ACTIVE_ELIGIBLE,
            PL_CHARGED_OFF_ELIGIBLE,
            SC_ACTIVE_ELIGIBLE,
            SC_CHARGED_OFF_ELIGIBLE,
            PL_SC_NORMAL }
        const val VIEW_PAYMENT_PLAN_BUTTON = "viewPaymentPlanButton"
        const val MAKE_A_PAYMENT_BUTTON = "makeAPaymentButton"
        const val CANNOT_AFFORD_PAYMENT_BUTTON = "cannotAffordPaymentButton"
        const val PLAN_BUTTON_TYPE = "buttonType"
        const val APPLY_NOW_STATE = "state"
        const val ELIGIBILITY_PLAN = "eligibilityPlan"

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

        dialogButtonType = arguments?.getSerializable(PLAN_BUTTON_TYPE) as? ViewTreatmentPlanDialogButtonType
        state = arguments?.getSerializable(APPLY_NOW_STATE) as? ApplyNowState
        eligibilityPlan = arguments?.getSerializable(ELIGIBILITY_PLAN) as EligibilityPlan?

        mainButton?.apply {
            text = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ACTIVE_ELIGIBLE ||
                dialogButtonType ===  ViewTreatmentPlanDialogButtonType.SC_ACTIVE_ELIGIBLE ||
                dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_CHARGED_OFF_ELIGIBLE ||
                dialogButtonType ===  ViewTreatmentPlanDialogButtonType.SC_CHARGED_OFF_ELIGIBLE)
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
            visibility = if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ACTIVE_ELIGIBLE ||
                dialogButtonType ===  ViewTreatmentPlanDialogButtonType.SC_ACTIVE_ELIGIBLE ||
                dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_CHARGED_OFF_ELIGIBLE ||
                dialogButtonType ===  ViewTreatmentPlanDialogButtonType.SC_CHARGED_OFF_ELIGIBLE)
                    View.VISIBLE else View.GONE
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        viewTreatmentPlanDescriptionTextView?.apply {
            text =
                when(dialogButtonType) {
                    ViewTreatmentPlanDialogButtonType.PL_ACTIVE_ELIGIBLE -> {
                        payMyAccountViewModel.getCardDetail()?.account?.second?.amountOverdue?.let { totalAmountDue ->
                            activity?.resources?.getString(
                                R.string.treatment_plan_eligible_active_description_pl,
                                Utils.removeNegativeSymbol(
                                    CurrencyFormatter.formatAmountToRandAndCent(
                                        totalAmountDue
                                    )
                                )
                            )
                        }
                    }
                    ViewTreatmentPlanDialogButtonType.SC_ACTIVE_ELIGIBLE -> {
                        payMyAccountViewModel.getCardDetail()?.account?.second?.amountOverdue?.let { totalAmountDue ->
                            activity?.resources?.getString(
                                R.string.treatment_plan_eligible_active_description_sc,
                                Utils.removeNegativeSymbol(
                                    CurrencyFormatter.formatAmountToRandAndCent(
                                        totalAmountDue
                                    )
                                )
                            )
                        }
                    }
                    ViewTreatmentPlanDialogButtonType.PL_CHARGED_OFF_ELIGIBLE -> bindString(R.string.treatment_plan_eligible_charged_off_description_pl)

                    ViewTreatmentPlanDialogButtonType.SC_CHARGED_OFF_ELIGIBLE -> bindString(R.string.treatment_plan_eligible_charged_off_description_sc)

                    else -> bindString(R.string.view_treatment_plan_description)
                }
        }

        viewTreatmentPlanTitleTextView?.apply {
            text =
                when(dialogButtonType) {
                    ViewTreatmentPlanDialogButtonType.PL_ACTIVE_ELIGIBLE,
                    ViewTreatmentPlanDialogButtonType.SC_ACTIVE_ELIGIBLE ->
                        bindString(R.string.payment_overdue_label)
                    ViewTreatmentPlanDialogButtonType.PL_CHARGED_OFF_ELIGIBLE,
                    ViewTreatmentPlanDialogButtonType.SC_CHARGED_OFF_ELIGIBLE ->
                        bindString(R.string.remove_block_on_collection_dialog_title)

                    else -> bindString(R.string.payment_overdue_label)
                }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.mainButton -> {
                dismiss()
                if(dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_ACTIVE_ELIGIBLE ||
                    dialogButtonType ===  ViewTreatmentPlanDialogButtonType.SC_ACTIVE_ELIGIBLE ||
                    dialogButtonType ===  ViewTreatmentPlanDialogButtonType.PL_CHARGED_OFF_ELIGIBLE ||
                    dialogButtonType ===  ViewTreatmentPlanDialogButtonType.SC_CHARGED_OFF_ELIGIBLE)
                    setFragmentResult(mClassName, bundleOf(mClassName to MAKE_A_PAYMENT_BUTTON))
                else setFragmentResult(mClassName, bundleOf(mClassName to VIEW_PAYMENT_PLAN_BUTTON))
            }

            R.id.makePaymentButton, R.id.viewPaymentOptionsButton -> {
                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to MAKE_A_PAYMENT_BUTTON))
            }

            R.id.cannotAffordPaymentButton -> {
                val arguments = HashMap<String, String>()
                when(state){
                    ApplyNowState.STORE_CARD -> {
                        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC_ACTION
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC,
                            arguments,
                            activity)
                    }
                    ApplyNowState.PERSONAL_LOAN -> {
                        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL_ACTION
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL,
                            arguments,
                            activity)
                    }
                    ApplyNowState.SILVER_CREDIT_CARD,
                    ApplyNowState.GOLD_CREDIT_CARD,
                    ApplyNowState.BLACK_CREDIT_CARD, -> {
                        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC_ACTION
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC,
                            arguments,
                            activity)
                    }
                }

                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to CANNOT_AFFORD_PAYMENT_BUTTON,
                    ELIGIBILITY_PLAN to eligibilityPlan))
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}
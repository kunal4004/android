package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_treatment_plan_dialog_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.ProductOfferingStatus
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.DateFormatter
import za.co.woolworths.financial.services.android.util.DateFormatter.Companion.formatDateTOddMMMYYYY
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter.formatDateTOddMMMYYYY
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ViewTreatmentPlanDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private var account: Account? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = ViewTreatmentPlanDialogFragment::class.java.simpleName
    private var state: ApplyNowState? = null
    private var eligibilityPlan: EligibilityPlan? = null

    enum class TreatmentPlanType {
        VIEW, TAKE_UP, ELITE, NONE
    }

    companion object {
        const val VIEW_PAYMENT_PLAN_BUTTON = "viewPaymentPlanButton"
        const val MAKE_A_PAYMENT_BUTTON = "makeAPaymentButton"
        const val CANNOT_AFFORD_PAYMENT_BUTTON = "cannotAffordPaymentButton"
        const val APPLY_NOW_STATE = "state"
        const val ELIGIBILITY_PLAN = "eligibilityPlan"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountSignedInActivity) {
            showChatBubbleInterface = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_treatment_plan_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        state = arguments?.getSerializable(APPLY_NOW_STATE) as? ApplyNowState
        eligibilityPlan = arguments?.getSerializable(ELIGIBILITY_PLAN) as? EligibilityPlan
        account = payMyAccountViewModel.getCardDetail()?.account?.second


        val planType = when (eligibilityPlan?.actionText) {
            ActionText.TAKE_UP_TREATMENT_PLAN.value -> TreatmentPlanType.TAKE_UP
            ActionText.VIEW_TREATMENT_PLAN.value -> TreatmentPlanType.VIEW
            ActionText.VIEW_ELITE_PLAN.value -> TreatmentPlanType.ELITE
            else -> TreatmentPlanType.NONE
        }

        val isViewTreatmentPlan =
            eligibilityPlan?.actionText == ActionText.VIEW_TREATMENT_PLAN.value ||
                    eligibilityPlan?.actionText == ActionText.VIEW_ELITE_PLAN.value

        val isAccountChargedOff = payMyAccountViewModel.isAccountChargedOff()

        setTitleAndDescription()
        setListeners()
        setupMainButton(planType == TreatmentPlanType.TAKE_UP)
        setupMakePaymentButton(isViewTreatmentPlan)
        setupViewPaymentOptionsButton(isViewTreatmentPlan)
        setupCannotAffordPaymentButton(planType == TreatmentPlanType.TAKE_UP)
    }

    private fun setTitleAndDescription() {
        val isAccountChargedOff = payMyAccountViewModel.isAccountChargedOff()
        val applyNowState = payMyAccountViewModel.getApplyNowState()
        val presenter = ProductOfferingStatus(account)
        val isProductCC =
            applyNowState != ApplyNowState.STORE_CARD || applyNowState != ApplyNowState.PERSONAL_LOAN
        val amountOverdue = Utils.removeNegativeSymbol(
            CurrencyFormatter.formatAmountToRandAndCent(
                account?.amountOverdue ?: 0
            )
        )
        val paymentDueDate = account?.paymentDueDate ?: "N/A"

        val titleDesc = when (isAccountChargedOff) {
            true -> {
                when (isProductCC) {
                    true -> R.string.remove_block_on_collection_dialog_title to bindString(R.string.remove_block_on_collection_dialog_desc)
                    false -> R.string.remove_block_on_collection_dialog_title to bindString(R.string.remove_block_on_collection_dialog_desc)
                }
            }
            false -> {
                when (presenter.isViewTreatmentPlanSupported()
                        || presenter.isTakeUpTreatmentPlanJourneyEnabled()) {
                    true -> R.string.account_in_recovery_label to getViewTreatmentPlanDescription(state)
                    false -> R.string.payment_overdue_label to getString(
                        stringId = R.string.payment_overdue_error_desc,
                        amountOverdue
                    )
                }
            }
        }

        viewTreatmentPlanTitleTextView?.text = bindString(titleDesc.first)
        viewTreatmentPlanDescriptionTextView?.text = titleDesc.second

    }

    private fun setupMainButton(planType: Boolean) {
        mainButton?.text = if (planType)
            bindString(R.string.make_payment_now_button_label)
        else bindString(R.string.view_payment_plan_button_label)
    }

    private fun setupMakePaymentButton(isViewTreatmentPlan: Boolean) {
        makePaymentButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = if (isViewTreatmentPlan &&
                (state == ApplyNowState.PERSONAL_LOAN || state == ApplyNowState.STORE_CARD)
            ) View.VISIBLE else View.GONE
        }
    }

    private fun setupViewPaymentOptionsButton(isViewTreatmentPlan: Boolean) {
        viewPaymentOptionsButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = if (isViewTreatmentPlan &&
                (state == ApplyNowState.GOLD_CREDIT_CARD ||
                        state == ApplyNowState.BLACK_CREDIT_CARD ||
                        state == ApplyNowState.SILVER_CREDIT_CARD)
            )
                View.VISIBLE else View.GONE
        }
    }

    private fun setupCannotAffordPaymentButton(planType: Boolean) {
        cannotAffordPaymentButton?.visibility = if (planType) View.VISIBLE else View.GONE
    }

    fun setListeners() {
        mainButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        closeIconImageButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        makePaymentButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        viewPaymentOptionsButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        cannotAffordPaymentButton?.apply {
            setOnClickListener(this@ViewTreatmentPlanDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    @SuppressLint("VisibleForTests")
    private fun getViewTreatmentPlanDescription(
        state: ApplyNowState?
    ): String? {
        val descriptionId = when (state) {
            ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_desc
            ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_desc
            else -> R.string.account_in_recovery_desc
        }
        val paymentDueDate = account?.paymentDueDate ?: ""
        return activity?.resources?.getString(
            descriptionId,
            formatDateTOddMMMYYYY(paymentDueDate, toPattern = "dd MMMM yyyy")
        )
    }

    private fun getString(@StringRes stringId: Int, value: String): String {
        return requireActivity().getString(stringId, value)
    }

    override fun onClick(view: View?) {
        KotlinUtils.avoidDoubleClicks(view)
        when (view?.id) {

            R.id.mainButton -> {
                dismiss()
                setResult(
                    if (eligibilityPlan?.actionText == ActionText.TAKE_UP_TREATMENT_PLAN.value)
                        bundleOf(MAKE_A_PAYMENT_BUTTON to eligibilityPlan)
                    else bundleOf(VIEW_PAYMENT_PLAN_BUTTON to eligibilityPlan)
                )
            }

            R.id.makePaymentButton, R.id.viewPaymentOptionsButton -> {
                dismiss()
                setResult(bundleOf(MAKE_A_PAYMENT_BUTTON to MAKE_A_PAYMENT_BUTTON))
            }

            R.id.cannotAffordPaymentButton -> {
                setFirebaseEvent()
                dismiss()
                setResult(
                    bundleOf(
                        CANNOT_AFFORD_PAYMENT_BUTTON to CANNOT_AFFORD_PAYMENT_BUTTON,
                        ELIGIBILITY_PLAN to eligibilityPlan
                    )
                )
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    private fun setResult(bundle: Bundle) {
        setFragmentResult(mClassName, bundle)
    }

    private fun setFirebaseEvent() {
        val arguments = HashMap<String, String>()
        val event = when (state) {
            ApplyNowState.STORE_CARD ->
                FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC to
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC_ACTION
            ApplyNowState.PERSONAL_LOAN ->
                FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL to
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL_ACTION
            else ->
                FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC to
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC_ACTION
        }
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = event.second
        Utils.triggerFireBaseEvents(event.first, arguments, activity)
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}
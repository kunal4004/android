package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.annotation.SuppressLint
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
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ViewTreatmentPlanDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private lateinit var mTreatmentPlanImpl: ViewTreatmentPlanImpl
    private var account: Account? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = ViewTreatmentPlanDialogFragment::class.java.simpleName
    private var state: ApplyNowState? = null
    private var eligibilityPlan: EligibilityPlan? = null


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

        mTreatmentPlanImpl = ViewTreatmentPlanImpl(
            eligibilityPlan = eligibilityPlan,
            account = account,
            applyNowState = state,
            context = requireContext()
        )

        setTitleAndDescription()
        setListeners()
        setupMainButton()
        setupMakePaymentButton()
        setupViewPaymentOptionsButton()
        setupCannotAffordPaymentButton()
    }

    @SuppressLint("VisibleForTests")
    private fun setTitleAndDescription() {
        with(mTreatmentPlanImpl.getTitleAndDescription()) {
            viewTreatmentPlanTitleTextView?.text = bindString(first)
            viewTreatmentPlanDescriptionTextView?.text = second
        }
    }

    private fun setupMainButton() {
        mainButton?.text = mTreatmentPlanImpl.makePaymentPlanButtonLabel()
    }

    private fun setupMakePaymentButton() {
        makePaymentButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = mTreatmentPlanImpl.isMakePaymentButtonVisible()
        }
    }

    private fun setupViewPaymentOptionsButton() {
        viewPaymentOptionsButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = mTreatmentPlanImpl.isViewPaymentOptionsButtonVisible()
        }
    }

    private fun setupCannotAffordPaymentButton() {
        cannotAffordPaymentButton?.visibility =
            mTreatmentPlanImpl.isCannotAffordPaymentButtonVisible()
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
        val event = mTreatmentPlanImpl.cannotAffordPaymentFirebaseEvent()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = event.second
        Utils.triggerFireBaseEvents(event.first, arguments, activity)
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}
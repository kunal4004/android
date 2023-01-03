package za.co.woolworths.financial.services.android.ui.fragments.account.remove_dc_block

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RemoveBlockOnCollectionDialogBinding
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.ELITE_PLAN
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanImpl
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.eliteplan.TakeUpPlanUtil

class RemoveBlockOnCollectionDialogFragment : AppCompatDialogFragment(R.layout.remove_block_on_collection_dialog), View.OnClickListener {

    private lateinit var binding: RemoveBlockOnCollectionDialogBinding
    private lateinit var mTreatmentPlanImpl: ViewTreatmentPlanImpl
    private val mClassName = RemoveBlockOnCollectionDialogFragment::class.java.simpleName
    private var eligibilityPlan: EligibilityPlan? = null
    private var state: ApplyNowState? = null

    companion object {
        const val ARREARS_PAY_NOW_BUTTON = "payNowButton"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RemoveBlockOnCollectionDialogBinding.bind(view)

        state =
            arguments?.getSerializable(ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE) as? ApplyNowState
        eligibilityPlan =
            arguments?.getSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as? EligibilityPlan

        val presenter = (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter
        mTreatmentPlanImpl = ViewTreatmentPlanImpl(
            eligibilityPlan = eligibilityPlan,
            account = presenter?.getAccount(),
            applyNowState = state)

        val titleDesc = mTreatmentPlanImpl.getTitleAndDescription()
        binding.accountInArrearsTitleTextView?.text = bindString(titleDesc.first)
        binding.accountInArrearsDescriptionTextView?.text = titleDesc.second

        binding.cannotAffordPaymentButton?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionDialogFragment)
            when (eligibilityPlan?.planType) {
                ELITE_PLAN -> visibility = when (state) {
                    ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> VISIBLE
                    else -> GONE
                }
            }
        }
        binding.payNowButton?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
            if (enableElitePlanForCC()) {
                when (eligibilityPlan?.actionText) {
                    ActionText.VIEW_ELITE_PLAN.value -> {
                        text = bindString(R.string.view_your_payment_plan)
                    }
                    ActionText.START_NEW_ELITE_PLAN.value -> {
                        text = bindString(R.string.get_help_repayment)
                    }
                }
            }

        }

        binding.closeIconImageButton?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.payNowButton -> {
                if (enableElitePlanForCC()) {
                    cannotAffordClickHandler()
                } else {
                    dismiss()
                    setFragmentResult(mClassName, bundleOf(mClassName to ARREARS_PAY_NOW_BUTTON))
                }
            }
            R.id.cannotAffordPaymentButton -> {
                cannotAffordClickHandler()
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    private fun cannotAffordClickHandler() {
        when (eligibilityPlan?.actionText) {
            ActionText.VIEW_ELITE_PLAN.value -> {
                KotlinUtils.openTreatmentPlanUrl(activity, eligibilityPlan)
            }
            else -> {
                activity?.apply {
                    state?.let {
                        TakeUpPlanUtil.takeUpPlanEventLog(it, this)
                    }
                }
                setFragmentResult(
                    mClassName, bundleOf(
                        ViewTreatmentPlanDialogFragment.CANNOT_AFFORD_PAYMENT_BUTTON to ViewTreatmentPlanDialogFragment.CANNOT_AFFORD_PAYMENT_BUTTON,
                        ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN to eligibilityPlan
                    )
                )
                openSetupPaymentPlanPage()
            }
        }
        dismiss()
    }

    private fun openSetupPaymentPlanPage() {
        activity?.apply {
            val intent = Intent(context, GetAPaymentPlanActivity::class.java)
            intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, eligibilityPlan)
            startActivityForResult(intent, AccountsOptionFragment.REQUEST_ELITEPLAN)
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        }
    }

    private fun enableElitePlanForCC(): Boolean {
        return eligibilityPlan?.planType.equals(ELITE_PLAN) &&
                (state != ApplyNowState.PERSONAL_LOAN && state != ApplyNowState.STORE_CARD)
    }

    private fun setupMakePaymentButton() {
        binding.payNowButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = mTreatmentPlanImpl.isMakePaymentButtonVisible()
            setOnClickListener(this@RemoveBlockOnCollectionDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

}
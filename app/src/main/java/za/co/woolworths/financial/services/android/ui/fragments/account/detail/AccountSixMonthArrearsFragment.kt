package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountSixMonthArrearsFragmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.ELITE_PLAN
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.displayLabel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.eliteplan.EligibilityImpl
import za.co.woolworths.financial.services.android.util.eliteplan.TakeUpPlanUtil

class AccountSixMonthArrearsFragment : Fragment(R.layout.account_six_month_arrears_fragment), EligibilityImpl {

    private lateinit var binding: AccountSixMonthArrearsFragmentBinding
    private var mApplyNowAccountKeyPair: Pair<Int, Int>? = null
    private var isViewTreatmentPlanSupported: Boolean = false
    private var mAccountPresenter: AccountSignedInPresenterImpl? = null

    companion object {
        const val IS_VIEW_TREATMENT_PLAN = "IS_VIEW_TREATMENT_PLAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, "")
        mApplyNowAccountKeyPair = Gson().fromJson(account, object : TypeToken<Pair<Int, Int>>() {}.type)
        isViewTreatmentPlanSupported = arguments?.getBoolean(IS_VIEW_TREATMENT_PLAN, false) ?: false

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AccountSixMonthArrearsFragmentBinding.bind(view)
        mAccountPresenter = (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter
        mAccountPresenter?.eligibilityImpl = this

        with(binding) {
            hideCardTextViews()
            setTitleAndCardTypeAndButton()
            callTheCallCenterButton?.setOnClickListener { Utils.makeCall("0861502020") }
            callTheCallCenterUnderlinedButton?.setOnClickListener { Utils.makeCall("0861502020") }
            viewTreatmentPlansButton?.setOnClickListener {
                when (mAccountPresenter?.getEligibilityPlan()?.planType) {
                    ELITE_PLAN -> {
                        elitePlanHandling()
                    }
                    else -> {
                        val outSystemBuilder = OutSystemBuilder(activity, ProductGroupCode.CC)
                        outSystemBuilder.build()
                    }
                }
            }
            navigateBackImageButton?.setOnClickListener { activity?.onBackPressed() }

            includeAccountDetailHeaderView.bottomView?.visibility = INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        when (mAccountPresenter?.getEligibilityPlan()?.planType) {
            ELITE_PLAN -> binding.setElitePlanViews(mAccountPresenter?.getEligibilityPlan())
        }
    }

    private fun AccountSixMonthArrearsFragmentBinding.hideCardTextViews() {
        with(includeAccountDetailHeaderView) {
            context?.let { color -> ContextCompat.getColor(color, R.color.white) }
                ?.let { color -> includeAccountDetailHeaderView?.root?.setBackgroundColor(color) }
            myCardTextView?.visibility = GONE
            myCardDetailTextView?.visibility = GONE
            userNameTextView?.visibility = GONE
            includeManageMyCard.imLogoIncreaseLimit?.visibility = GONE
            includeManageMyCard.manageMyCardTextView?.visibility = GONE
            includeManageMyCard.manageMyCardImageView?.visibility = GONE
            manageCardDivider?.background = null
            includeManageMyCard?.root?.layoutParams?.apply {
                height = 0
            }
        }
    }

    private fun AccountSixMonthArrearsFragmentBinding.setTitleAndCardTypeAndButton() {
        mApplyNowAccountKeyPair?.first?.let { resourceId ->
            with(includeAccountDetailHeaderView.cardDetailImageShimmerFrameLayout){
               if (isVisible){
                   stopShimmer()
                   setShimmer(null)
               }
            }

            includeAccountDetailHeaderView.cardDetailImageView?.setImageResource(
                resourceId
            )
        }
        mApplyNowAccountKeyPair?.second?.let { resourceId ->
            toolbarTitleTextView?.text = bindString(resourceId)
            if (isViewTreatmentPlanSupported && (resourceId == R.string.black_credit_card_title ||
                        resourceId == R.string.gold_credit_card_title ||
                        resourceId == R.string.silver_credit_card_title)
            ) {
                arrearsDescTextView?.text = bindString(R.string.account_arrears_cc_description)
                callTheCallCenterButton?.visibility = GONE
                viewTreatmentPlansButton?.visibility = VISIBLE
                callTheCallCenterUnderlinedButton?.apply {
                    paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    visibility = VISIBLE
                }
            } else {
                showCallUsButton()
            }
        }
    }

    override fun eligibilityResponse(eligibilityPlan: EligibilityPlan?) {
        eligibilityPlan.let {
            if (it?.planType.equals(ELITE_PLAN)) {
                binding.setElitePlanViews(eligibilityPlan)
            }
        }
    }

    override fun eligibilityFailed() {
        binding.showCallUsButton()
    }

    private fun AccountSixMonthArrearsFragmentBinding.showCallUsButton() {
        arrearsDescTextView?.text =
            activity?.resources?.getString(R.string.account_arrears_cc_description)
        callTheCallCenterButton?.visibility = VISIBLE
        viewTreatmentPlansButton?.visibility = GONE
        callTheCallCenterUnderlinedButton?.visibility = GONE
    }

    private fun AccountSixMonthArrearsFragmentBinding.setElitePlanViews(eligibilityPlan: EligibilityPlan?) {
        arrearsDescTextView?.text = bindString(R.string.account_arrears_cc_description)
        callTheCallCenterButton?.visibility = GONE
        viewTreatmentPlansButton.visibility = VISIBLE
        callTheCallCenterUnderlinedButton?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            visibility = VISIBLE
        }
        when (eligibilityPlan?.actionText) {
            ActionText.VIEW_ELITE_PLAN.value -> {
                callTheCallCenterUnderlinedButton?.apply {
                    visibility = GONE
                    isEnabled = false
                }
                viewTreatmentPlansButton.text = requireContext().displayLabel()
            }
            ActionText.START_NEW_ELITE_PLAN.value -> {
                viewTreatmentPlansButton.text = bindString(R.string.get_help_repayment)
            }
        }
    }

    private fun elitePlanHandling() {

        when (mAccountPresenter?.getEligibilityPlan()?.actionText) {
            ActionText.START_NEW_ELITE_PLAN.value -> {
                activity?.apply {
                    TakeUpPlanUtil.takeUpPlanEventLog(mAccountPresenter?.getMyAccountCardInfo()?.first, this)
                }
                openSetupPaymentPlanPage()
            }

            ActionText.VIEW_ELITE_PLAN.value -> {
                KotlinUtils.openTreatmentPlanUrl(activity, mAccountPresenter?.getEligibilityPlan())
            }
        }
    }

    private fun openSetupPaymentPlanPage() {
        activity?.apply {
            val intent = Intent(context, GetAPaymentPlanActivity::class.java)
            intent.putExtra(
                ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN,
                mAccountPresenter?.getEligibilityPlan()
            )
            startActivityForResult(intent, AccountsOptionFragment.REQUEST_ELITEPLAN)
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.account.remove_dc_block

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_in_arrears_layout.*
import kotlinx.android.synthetic.main.remove_block_dc_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigPayMyAccount
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.ELITE_PLAN
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.displayLabel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.eliteplan.EligibilityImpl
import za.co.woolworths.financial.services.android.util.eliteplan.PMApiStatusImpl
import za.co.woolworths.financial.services.android.util.eliteplan.TakeUpPlanUtil
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType

class RemoveBlockOnCollectionFragment : Fragment(), View.OnClickListener, EligibilityImpl,PMApiStatusImpl {

    lateinit var navController: NavController
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    private var accountData: Pair<ApplyNowState, Account>? = null
    private var mAccountPresenter: AccountSignedInPresenterImpl? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.remove_block_dc_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAccountPresenter = (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter
        accountData = mAccountPresenter?.getMyAccountCardInfo()
        mAccountPresenter?.eligibilityImpl = this
        mAccountPresenter?.pmaStatusImpl = this

        when (accountData?.first) {
            ApplyNowState.PERSONAL_LOAN -> {
                removeBlockBackgroundConstraintLayout?.setBackgroundResource(R.drawable.personal_loan_background)
            }
            ApplyNowState.STORE_CARD -> {
                removeBlockBackgroundConstraintLayout?.setBackgroundResource(R.drawable.store_card_background)
            }
            else -> {
            }
        }

        val account = accountData?.second
        currentBalanceAmountTextview?.text =
            Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(account?.currentBalance))
        totalAmountDueAmountTextview?.text =
            Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(account?.totalAmountDue))

        setPushViewDownAnimation(incRecentTransactionButton)
        setPushViewDownAnimation(incViewStatementButton)
        setPushViewDownAnimation(incPayMyAccountButton)
        setPushViewDownAnimation(accountInArrearsTextView)
        setPushViewDownAnimation(navigateBackImageButton)
        setPushViewDownAnimation(toolbarTitleTextView)
        setPushViewDownAnimation(infoIconImageView)
        setPushViewDownAnimation(helpWithPayment)
        helpWithPaymentView.visibility =
            if (mAccountPresenter?.getEligibilityPlan()?.planType.equals(ELITE_PLAN)) VISIBLE else GONE
        val contactCallCenter =
            WSpannableStringBuilder(bindString(R.string.contact_the_call_centre_now))
        contactCallCenter.makeStringInteractable("0861502020", LinkType.PHONE)
        contactCallCenter.makeStringUnderlined("0861502020")
        setUnderlineText(contactCallCenter.build(), contactCallCenterNowTextview)

        stopProgress()

        setFragmentResultListener(RemoveBlockOnCollectionDialogFragment::class.java.simpleName) { _, bundle ->
            CoroutineScope(Dispatchers.Main).launch {
                delay(AppConstant.DELAY_100_MS)
                when (bundle.getString(
                    RemoveBlockOnCollectionDialogFragment::class.java.simpleName,
                    "N/A"
                )) {
                    RemoveBlockOnCollectionDialogFragment.ARREARS_PAY_NOW_BUTTON -> {
                        accountData?.apply {
                            when (first) {
                                ApplyNowState.PERSONAL_LOAN -> navigatePayMyAccountPersonalLoan()
                                ApplyNowState.STORE_CARD -> navigatePayMyAccountStoreCard()
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.incRecentTransactionButton -> navigateToRecentTransactionActivity()
            R.id.incViewStatementButton -> navigateToStatementActivity()
            R.id.incPayMyAccountButton -> {
                accountData?.apply {
                    when (first) {
                        ApplyNowState.PERSONAL_LOAN -> navigatePayMyAccountPersonalLoan()
                        ApplyNowState.STORE_CARD -> navigatePayMyAccountStoreCard()
                        else -> {
                        }
                    }
                }
            }
            R.id.accountInArrearsTextView -> {
            }
            R.id.navigateBackImageButton -> activity?.onBackPressed()
            R.id.toolbarTitleTextView -> {
            }
            R.id.infoIconImageView -> navigateToCardInformation()
            R.id.helpWithPayment -> {
                elitePlanHandling()
            }
        }
    }

    private fun elitePlanHandling() {
        when (mAccountPresenter?.getEligibilityPlan()?.actionText) {
            ActionText.START_NEW_ELITE_PLAN.value -> {
                activity?.apply {
                    payMyAccountViewModel.getApplyNowState().let {
                        TakeUpPlanUtil.takeUpPlanEventLog(it!!, this)
                    }
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

    private fun setPushViewDownAnimation(view: View?) {
        view?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }
    private fun navigateToDeepLinkView() {
        if (activity is AccountSignedInActivity) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_100_MS)
                (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.apply {
                    val deepLinkingObject = getDeepLinkData()
                    when (deepLinkingObject?.get("feature")?.asString) {
                        AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT -> {
                            deleteDeepLinkData()
                            incPayMyAccountButton?.performClick()
                        }
                    }
                }
            }
        }
    }

    private fun navigatePayMyAccountStoreCard() {
        payMyAccountViewModel.resetAmountEnteredToDefault()

        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC,
                this
            )
        }

        if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
            try {
                if (navController.currentDestination?.id == R.id.storeCardFragment) {
                    navController.navigate(R.id.payMyAccountRetryErrorFragment)
                }
            } catch (ex: IllegalStateException) {
                FirebaseManager.logException(ex)
            }
            return
        }

        navigateToPayMyAccount {
            try {
                navigateSafelyWithNavController(RemoveBlockOnCollectionFragmentDirections.actionRemoveBlockDCFragmentToEnterPaymentAmountDetailFragment())
            } catch (ex: IllegalStateException) {
                FirebaseManager.logException(ex)
            }
        }
    }

    private fun navigatePayMyAccountPersonalLoan() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_PL,
                this
            )
        }

        if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
            navController.navigate(R.id.payMyAccountRetryErrorFragment)
            return
        }

        payMyAccountViewModel.resetAmountEnteredToDefault()

        navigateToPayMyAccount {
            navigateSafelyWithNavController(RemoveBlockOnCollectionFragmentDirections.actionRemoveBlockDCFragmentToEnterPaymentAmountDetailFragment())
        }
    }

    private fun navigateToPayMyAccount(openCardOptionsDialog: () -> Unit) {
        val payMyAccountOption: ConfigPayMyAccount? = AppConfigSingleton.mPayMyAccount
        val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
        val payUMethodType = payMyAccountViewModel.getCardDetail()?.payuMethodType
        when {
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> openCardOptionsDialog()
            else -> navigateToPayMyAccountActivity()
        }
    }

    private fun navigateToPayMyAccountActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity ->
            ActivityIntentNavigationManager.presentPayMyAccountActivity(
                activity,
                payMyAccountViewModel.getCardDetail()
            )
        }
    }

    private fun navigateToRecentTransactionActivity() {
        activity?.let { activity ->
            val applyNowState = accountData?.first
            val propertyName = when (applyNowState) {
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS
                else -> ""
            }
            requireActivity().apply { Utils.triggerFireBaseEvents(propertyName, this) }
            accountData?.second?.apply {
                val intent = Intent(activity, WTransactionsActivity::class.java)
                intent.putExtra(
                    BundleKeysConstants.PRODUCT_OFFERINGID,
                    productOfferingId.toString()
                )
                intent.putExtra(
                    ChatFragment.ACCOUNTS,
                    Gson().toJson(Pair(applyNowState, this))
                )
                intent.putExtra("cardType", productGroupCode?.uppercase())
                activity.startActivityForResult(intent, 0)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    private fun navigateToStatementActivity() {
        accountData?.apply {

            val applyNowState = accountData?.first
            val propertyName = when (applyNowState) {
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANSTATEMENTS
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS
                else -> ""
            }
            activity?.apply { Utils.triggerFireBaseEvents(propertyName, this) }

            activity?.apply {
                val openStatement = Intent(this, StatementActivity::class.java)
                openStatement.putExtra(
                    ChatFragment.ACCOUNTS,
                    Gson().toJson(
                        Pair(
                            applyNowState,
                            second
                        )
                    )
                )
                startActivity(openStatement)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    private fun navigateToCardInformation() {
        activity ?: return
        val helpIcon = mAccountPresenter?.getCardProductInformation(true)

        val cardInformationHelpActivity = Intent(activity, CardInformationHelpActivity::class.java)
        cardInformationHelpActivity.putExtra(
            CardInformationHelpActivity.HELP_INFORMATION,
            Gson().toJson(helpIcon)
        )
        activity?.startActivityForResult(
            cardInformationHelpActivity,
            AccountSignedInActivity.REQUEST_CODE_ACCOUNT_INFORMATION
        )
        activity?.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
    }

    private fun setUnderlineText(howToUseSpannableContent: Spannable, textView: TextView?) {
        textView?.text = howToUseSpannableContent
        textView?.movementMethod = LinkMovementMethod.getInstance()
        textView?.highlightColor = Color.TRANSPARENT
    }

    private fun fragmentAlreadyAdded(): Boolean {
        if (!isAdded) return true
        return false
    }

    fun stopProgress() {
        viewPaymentOptionImageShimmerLayout?.setShimmer(null)
        viewPaymentOptionImageShimmerLayout?.stopShimmer()

        viewPaymentOptionTextShimmerLayout?.setShimmer(null)
        viewPaymentOptionTextShimmerLayout?.stopShimmer()
    }

    override fun eligibilityResponse(eligibilityPlan: EligibilityPlan?) {
        eligibilityPlan.let { plan ->
            helpWithPayment.text =  when (plan?.actionText.equals(ActionText.VIEW_ELITE_PLAN.value, ignoreCase = true)) {
                true -> requireContext().displayLabel()
                false ->   bindString( R.string.get_help_repayment)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_1000_MS)
                helpWithPaymentView.visibility = if (plan?.planType.equals(ELITE_PLAN)) VISIBLE else GONE
            }
        }
    }

    override fun eligibilityFailed() {
        helpWithPaymentView.visibility = GONE
    }

    override fun pmaSuccess() {
        navigateToDeepLinkView()
    }
}
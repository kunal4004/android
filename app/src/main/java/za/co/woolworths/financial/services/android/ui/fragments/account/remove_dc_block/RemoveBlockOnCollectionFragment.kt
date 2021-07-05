package za.co.woolworths.financial.services.android.ui.fragments.account.remove_dc_block

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_in_arrears_layout.*
import kotlinx.android.synthetic.main.remove_block_dc_fragment.*
import kotlinx.android.synthetic.main.remove_block_dc_fragment.incPayMyAccountButton
import kotlinx.android.synthetic.main.remove_block_dc_fragment.incRecentTransactionButton
import kotlinx.android.synthetic.main.remove_block_dc_fragment.incViewStatementButton
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PayMyAccount
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.safeNavigateFromNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType
import java.util.*

class RemoveBlockOnCollectionFragment : Fragment(), View.OnClickListener {

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

        val contactCallCenter = WSpannableStringBuilder(bindString(R.string.contact_the_call_centre_now))
        contactCallCenter.makeStringInteractable("0861502020", LinkType.PHONE)
        contactCallCenter.makeStringUnderlined("0861502020")
        setUnderlineText(contactCallCenter.build(), contactCallCenterNowTextview)

        stopProgress()

        setFragmentResultListener(RemoveBlockOnCollectionDialogFragment::class.java.simpleName) { _, bundle ->
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(RemoveBlockOnCollectionDialogFragment::class.java.simpleName, "N/A")) {
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
        }
    }

    private fun setPushViewDownAnimation(view: View?) {
        view?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    private fun navigatePayMyAccountStoreCard() {
        payMyAccountViewModel.resetAmountEnteredToDefault()

        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC, this) }

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
                safeNavigateFromNavController(RemoveBlockOnCollectionFragmentDirections.actionRemoveBlockDCFragmentToEnterPaymentAmountDetailFragment())
            } catch (ex: IllegalStateException) {
                FirebaseManager.logException(ex)
            }
        }
    }

    private fun navigatePayMyAccountPersonalLoan() {
        activity?.apply {Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_PL, this) }

        if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
            navController.navigate(R.id.payMyAccountRetryErrorFragment)
            return
        }

        payMyAccountViewModel.resetAmountEnteredToDefault()

        navigateToPayMyAccount {
            safeNavigateFromNavController(RemoveBlockOnCollectionFragmentDirections.actionRemoveBlockDCFragmentToEnterPaymentAmountDetailFragment())
        }
    }

    private fun navigateToPayMyAccount(openCardOptionsDialog: () -> Unit) {
        val payMyAccountOption: PayMyAccount? = WoolworthsApplication.getPayMyAccountOption()
        val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
        val payUMethodType = payMyAccountViewModel.getCardDetail()?.payuMethodType
        when {
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> openCardOptionsDialog()
            else -> navigateToPayMyAccountActivity()
        }
    }

    private fun navigateToPayMyAccountActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    private fun navigateToRecentTransactionActivity() {
        activity?.let { activity ->
            val applyNowState = accountData?.first
            val propertyName = when (applyNowState) {
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS
                else -> ""
            }
            activity?.apply {Utils.triggerFireBaseEvents(propertyName, this) }
            accountData?.second?.apply {
                val intent = Intent(activity, WTransactionsActivity::class.java)
                intent.putExtra("productOfferingId", productOfferingId.toString())
                intent.putExtra(
                    ChatFragment.ACCOUNTS,
                    Gson().toJson(Pair(applyNowState, this))
                )
                intent.putExtra("cardType", productGroupCode?.toUpperCase())
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
            activity?.apply {Utils.triggerFireBaseEvents(propertyName, this) }

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
        cardInformationHelpActivity.putExtra(CardInformationHelpActivity.HELP_INFORMATION, Gson().toJson(helpIcon))
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
}
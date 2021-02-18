package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.android.synthetic.main.view_statement_button.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IAvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.IBottomSheetBehaviourPeekHeightListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.PayMyAccount
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity.Companion.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.views.actionsheet.AccountsErrorHandlerFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.net.ConnectException

open class AvailableFundFragment : Fragment(), IAvailableFundsContract.AvailableFundsView {
    private lateinit var mAvailableFundPresenter: AvailableFundsPresenterImpl
    private lateinit var bottomSheetBehaviourPeekHeightListener: IBottomSheetBehaviourPeekHeightListener
    var isQueryPayUPaymentMethodComplete: Boolean = false
    lateinit var navController: NavController

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAvailableFundPresenter = AvailableFundsPresenterImpl(this, AvailableFundsModelImpl())
        mAvailableFundPresenter?.setBundle(arguments)
    }

    @Throws(RuntimeException::class)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetBehaviourPeekHeightListener) {
            bottomSheetBehaviourPeekHeightListener = context
        } else {
            throw RuntimeException("AvailableFundsFragment context value $context must implement BottomSheetBehaviourPeekHeightListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_available_fund_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()

        setPushViewDownAnimation(incRecentTransactionButton)
        setPushViewDownAnimation(incViewStatementButton)
        setPushViewDownAnimation(incPayMyAccountButton)

        val bottomViewGuideline = view.findViewById<Guideline>(R.id.bottomGuide)
        val constParam: ConstraintLayout.LayoutParams = bottomViewGuideline.layoutParams as ConstraintLayout.LayoutParams
        constParam.guidePercent = if ((activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.isAccountInArrearsState() == true) {
            paymentOverdueGroup?.visibility = VISIBLE
            0.8f
        } else {
            paymentOverdueGroup?.visibility = GONE
            0.7f
        }
        bottomViewGuideline.layoutParams = constParam

        availableFundBackground?.post {
            val dm = DisplayMetrics()
            (activity as? AppCompatActivity)?.windowManager?.defaultDisplay?.getMetrics(dm)
            val deviceHeight = dm.heightPixels
            val location = IntArray(2)
            bottomGuide?.getLocationOnScreen(location)
            val bottomGuidelineVerticalPosition = location[1]
            val displayBottomSheetBehaviorWithinRemainingHeight = deviceHeight - bottomGuidelineVerticalPosition
            bottomSheetBehaviourPeekHeightListener?.onBottomSheetPeekHeight(displayBottomSheetBehaviorWithinRemainingHeight)

        }

        activity?.let { act ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(act, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection || !isQueryPayUPaymentMethodComplete) {
                        true -> queryPaymentMethod()
                        else -> ErrorHandlerView(act).showToast()
                    }

                }
            })
        }

        payMyAccountViewModel.getNavigationResult().observe(viewLifecycleOwner) { result ->
            when (result) {
                PayMyAccountViewModel.OnBackNavigation.RETRY -> {
                    activity?.runOnUiThread {
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
                        queryPaymentMethod()
                    }
                }
                else -> return@observe
            }
        }
    }

    fun queryPaymentMethod() {
        when (!payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
            true -> {
                initShimmer()
                startProgress()

                val cardInfo = payMyAccountViewModel.getCardDetail()
                val account = mAvailableFundPresenter?.getAccountDetail()
                val amountEntered = account?.second?.amountOverdue?.let { amountDue -> Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCent(amountDue)) }
                val payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                val paymentMethodList = cardInfo?.paymentMethodList

                val card = PMACardPopupModel(amountEntered, paymentMethodList, account, payUMethodType)
                payMyAccountViewModel.setPMACardInfo(card)

                payMyAccountViewModel.queryServicePayUPaymentMethod(
                        { // onSuccessResult
                            if (!isAdded) return@queryServicePayUPaymentMethod
                            stopProgress()
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true

                        }, { onSessionExpired ->
                    if (!isAdded) return@queryServicePayUPaymentMethod
                    activity?.let {
                        stopProgress()
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, onSessionExpired, it)

                    }
                }, { // on unknown http error / general error
                    if (!isAdded) return@queryServicePayUPaymentMethod
                    stopProgress()
                    payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true

                }, { throwable ->
                    if (!isAdded) return@queryServicePayUPaymentMethod
                    activity?.runOnUiThread {
                        stopProgress()
                    }
                    payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                    if (throwable is ConnectException) {
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
                    }
                })
            }
            false -> return
        }
    }


    override fun setPushViewDownAnimation(view: View) {
        AnimationUtilExtension.animateViewPushDown(view)
    }

    override fun onABSACreditCardFailureHandler(error: Throwable?) {
        activity?.let { activity ->
            activity.runOnUiThread {
                if (error is ConnectException) {
                    ErrorHandlerView(activity).showToast()
                }
            }
        }
    }

    private fun setUpView() {
        mAvailableFundPresenter?.getAccount()?.apply {
            activity?.apply {
                val availableFund = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCentNoSpace(availableFunds), 1))
                val currentBalance = Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(currentBalance))
                val creditLimit = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCentWithSpace(creditLimit), 1))
                val paymentDueDate = paymentDueDate?.let { paymentDueDate -> WFormatter.addSpaceToDate(WFormatter.newDateFormat(paymentDueDate)) }
                        ?: "N/A"
                val amountOverdue = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCentWithSpace(amountOverdue), 1))

                val totalAmountDueAmount = Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(totalAmountDue))

                availableFundAmountTextView?.text = availableFund
                currentBalanceAmountTextView?.text = currentBalance
                creditLimitAmountTextView?.text = creditLimit
                totalAmountDueAmountTextView?.text = totalAmountDueAmount
                nextPaymentDueDateTextView?.text = paymentDueDate
                amountPayableNowAmountTextView?.text = amountOverdue
            }
        }
    }

    override fun navigateToStatementActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            val openStatement = Intent(this, StatementActivity::class.java)
            openStatement.putExtra(ACCOUNTS, Gson().toJson(Pair(mAvailableFundPresenter?.getApplyNowState(), mAvailableFundPresenter?.getAccount())))
            startActivity(openStatement)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun navigateToPaymentOptionsActivity() {
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    override fun navigateToPayMyAccountActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    override fun navigateToOnlineBankingActivity(creditCardNumber: String, isRegistered: Boolean) {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            val openABSAOnlineBanking = Intent(this, ABSAOnlineBankingRegistrationActivity::class.java)
            openABSAOnlineBanking.putExtra(ABSAOnlineBankingRegistrationActivity.SHOULD_DISPLAY_LOGIN_SCREEN, isRegistered)
            openABSAOnlineBanking.putExtra("creditCardToken", creditCardNumber)
            openABSAOnlineBanking.putExtra(ACCOUNTS, Gson().toJson(mAvailableFundPresenter?.getBundle()))
            startActivityForResult(openABSAOnlineBanking, ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun displayCardNumberNotFound() {
        if (fragmentAlreadyAdded()) return
        if ((activity as? AccountSignedInActivity)?.bottomSheetIsExpanded() == true) return
        try {
            val accountsErrorHandlerFragment = activity?.resources?.getString(R.string.card_number_not_found)?.let { AccountsErrorHandlerFragment.newInstance(it) }
            activity?.supportFragmentManager?.let { supportFragmentManager -> accountsErrorHandlerFragment?.show(supportFragmentManager, AccountsErrorHandlerFragment::class.java.simpleName) }
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

    override fun handleUnknownHttpResponse(desc: String?) {
        if (fragmentAlreadyAdded()) return
        activity?.supportFragmentManager?.let { fragmentManager ->
            Utils.showGeneralErrorDialog(fragmentManager, desc)
        }
    }

    override fun handleSessionTimeOut(stsParams: String) {
        if (fragmentAlreadyAdded()) return
        (activity as? AccountSignedInActivity)?.let { accountSignedInActivity -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams, accountSignedInActivity) }
    }

    override fun showABSAServiceGetUserCreditCardTokenProgressBar() {
        if (fragmentAlreadyAdded()) return
        statementProgressBarGroup?.visibility = VISIBLE
    }

    override fun hideABSAServiceGetUserCreditCardTokenProgressBar() {
        if (fragmentAlreadyAdded()) return
        activity?.runOnUiThread {
            statementProgressBarGroup?.visibility = GONE
        }
    }

    private fun fragmentAlreadyAdded(): Boolean {
        if (!isAdded) return true
        return false
    }

    override fun onDestroy() {
        mAvailableFundPresenter?.onDestroy()
        super.onDestroy()
    }

    override fun navigateToLoanWithdrawalActivity() {
        activity?.apply {
            val intentWithdrawalActivity = Intent(this, LoanWithdrawalActivity::class.java)
            intentWithdrawalActivity.putExtra("account_info", Gson().toJson(mAvailableFundPresenter?.getAccount()))
            startActivityForResult(intentWithdrawalActivity, 0)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun navigateToRecentTransactionActivity(cardType: String) {
        activity?.let { activity ->
            mAvailableFundPresenter?.getAccount()?.apply {
                val intent = Intent(activity, WTransactionsActivity::class.java)
                intent.putExtra("productOfferingId", productOfferingId.toString())
                if (cardType == AccountsProductGroupCode.CREDIT_CARD.groupCode && accountNumber?.isNotEmpty() == true) {
                    intent.putExtra("accountNumber", accountNumber.toString())
                }
                intent.putExtra(ACCOUNTS, Gson().toJson(Pair(mAvailableFundPresenter?.getApplyNowState(), this)))
                intent.putExtra("cardType", cardType)
                activity.startActivityForResult(intent, 0)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    override fun navigateToABSAStatementActivity() {
        activity?.apply {
            FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS)
            if (NetworkManager().isConnectedToNetwork(this)) {
                mAvailableFundPresenter?.queryABSAServiceGetUserCreditCardToken()
            } else {
                ErrorHandlerView(this).showToast()
            }
        }
    }

    fun initShimmer() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        viewPaymentOptionImageShimmerLayout?.setShimmer(shimmer)
        viewPaymentOptionTextShimmerLayout?.setShimmer(shimmer)
    }

    private fun startProgress() {
        viewPaymentOptionImageShimmerLayout?.startShimmer()
        viewPaymentOptionTextShimmerLayout?.startShimmer()
    }

    fun stopProgress() {
        viewPaymentOptionImageShimmerLayout?.setShimmer(null)
        viewPaymentOptionImageShimmerLayout?.stopShimmer()

        viewPaymentOptionTextShimmerLayout?.setShimmer(null)
        viewPaymentOptionTextShimmerLayout?.stopShimmer()
    }

    fun navigateToPayMyAccount(openCardOptionsDialog: () -> Unit) {
        val payMyAccountOption: PayMyAccount? = WoolworthsApplication.getPayMyAccountOption()
        val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
        val payUMethodType = payMyAccountViewModel.getCardDetail()?.payuMethodType
        when {
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> openCardOptionsDialog()
            else -> navigateToPayMyAccountActivity()
        }
    }

    fun navigateToDeepLinkView() {
        if (activity is AccountSignedInActivity) {
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                val deepLinkingObject = (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.getDeepLinkData()
                when (deepLinkingObject?.get("feature")?.asString) {
                    DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT -> {
                        incViewStatementButton?.performClick()
                    }
                }
            }
        }
    }
}
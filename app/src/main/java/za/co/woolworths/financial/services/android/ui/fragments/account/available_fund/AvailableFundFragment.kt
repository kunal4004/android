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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import com.facebook.shimmer.Shimmer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.bottomGuide
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.android.synthetic.main.view_statement_button.*
import za.co.woolworths.financial.services.android.contracts.IAvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IBottomSheetBehaviourPeekHeightListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.PayMyAccount
import za.co.woolworths.financial.services.android.models.dto.PaymentAmountCard
import za.co.woolworths.financial.services.android.models.dto.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity.Companion.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.getNavigationResult
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.AccountsErrorHandlerFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType
import java.net.ConnectException

open class AvailableFundFragment : Fragment(), IAvailableFundsContract.AvailableFundsView {
    var mPaymentMethodsResponse: PaymentMethodsResponse? = null
    var mAvailableFundPresenter: AvailableFundsPresenterImpl? = null
    private var bottomSheetBehaviourPeekHeightListener: IBottomSheetBehaviourPeekHeightListener? = null
    var isQueryPayUPaymentMethodComplete: Boolean = false
    var navController: NavController? = null

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    var payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAvailableFundPresenter = AvailableFundsPresenterImpl(this, AvailableFundsModelImpl())
        mAvailableFundPresenter?.setBundle(arguments)
    }

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

        availableFundBackground?.post {
            val dm = DisplayMetrics()
            (activity as? AppCompatActivity)?.windowManager?.defaultDisplay?.getMetrics(dm)
            val deviceHeight = dm.heightPixels
            val location = IntArray(2)
            bottomGuide?.getLocationOnScreen(location)
            val bottomGuidelineVerticalPosition = location[1]
            val displayBottomSheetBehaviorWithinRemainingHeight = deviceHeight - bottomGuidelineVerticalPosition
            bottomSheetBehaviourPeekHeightListener?.onBottomSheetPeekHeight(displayBottomSheetBehaviorWithinRemainingHeight)

            getNavigationResult(Constant.GET_PAYMENT_METHOD_ERROR)?.observe(viewLifecycleOwner) { result ->
                isQueryPayUPaymentMethodComplete = false
                when (result) {
                    Constant.queryServiceGetPaymentMethod -> queryPaymentMethod()
                }
            }
        }

        activity?.let { act ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(act, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection) {
                        true -> queryPaymentMethod()
                        else -> ErrorHandlerView(act).showToast()
                    }

                }
            })
        }
    }

    fun queryPaymentMethod() {
        when ((mAvailableFundPresenter?.isPersonalLoanAndStoreCardVisible() == true && !isQueryPayUPaymentMethodComplete)) {
            true -> {
                initShimmer()
                startProgress()
                mAvailableFundPresenter?.queryPayUPaymentMethod()
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
                val availableFund = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newRandAmountFormatWithoutSpace(availableFunds), 1, this))
                val currentBalance = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(currentBalance))
                val creditLimit = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(creditLimit), 1, this))
                val paymentDueDate = paymentDueDate?.let { paymentDueDate -> WFormatter.addSpaceToDate(WFormatter.newDateFormat(paymentDueDate)) }
                        ?: "N/A"
                val totalAmountDueAmount = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(totalAmountDue))

                availableFundAmountTextView?.text = availableFund
                currentBalanceAmountTextView?.text = currentBalance
                creditLimitAmountTextView?.text = creditLimit
                totalAmountDueAmountTextView?.text = totalAmountDueAmount
                nextPaymentDueDateTextView?.text = paymentDueDate
            }
        }
    }

    override fun navigateToStatementActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS)
            val openStatement = Intent(this, StatementActivity::class.java)
            startActivity(openStatement)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun navigateToPaymentOptionsActivity(payMyAccountStartDestinationType: PayMyAccountStartDestinationType) {
        activity?.let { activity -> ScreenManager.presentPayMyAccountActivity(activity, mAvailableFundPresenter?.getBundle(), Gson().toJson(payMyAccountViewModel.getCardDetail()), payMyAccountStartDestinationType) }
    }

    override fun navigateToPayMyAccountActivity(payMyAccountStartDestinationType: PayMyAccountStartDestinationType) {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity -> ScreenManager.presentPayMyAccountActivity(activity, mAvailableFundPresenter?.getBundle(), Gson().toJson(payMyAccountViewModel.getCardDetail()), payMyAccountStartDestinationType) }
    }

    override fun navigateToOnlineBankingActivity(creditCardNumber: String, isRegistered: Boolean) {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            val openABSAOnlineBanking = Intent(this, ABSAOnlineBankingRegistrationActivity::class.java)
            openABSAOnlineBanking.putExtra(ABSAOnlineBankingRegistrationActivity.SHOULD_DISPLAY_LOGIN_SCREEN, isRegistered)
            openABSAOnlineBanking.putExtra("creditCardToken", creditCardNumber)
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
            Crashlytics.logException(ex)
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
                if (cardType == "CC" && accountNumber?.isNotEmpty() == true) {
                    intent.putExtra("accountNumber", accountNumber.toString())
                }
                intent.putExtra("cardType", cardType)
                activity.startActivityForResult(intent, 0)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    override fun navigateToABSAStatementActivity() {
        activity?.apply {
            if (NetworkManager().isConnectedToNetwork(this)) {
                mAvailableFundPresenter?.queryABSAServiceGetUserCreditCardToken()
            } else {
                ErrorHandlerView(this).showToast()
            }
        }
    }

    override fun onPayUMethodSuccess(paymentMethodsResponse: PaymentMethodsResponse?) {
        stopProgress()
        isQueryPayUPaymentMethodComplete = true
        paymentMethodsResponse?.apply {
            var account = mAvailableFundPresenter?.getAccountDetail()
            var amountEntered = account?.second?.totalAmountDue?.let { amountDue -> Utils.removeNegativeSymbol(WFormatter.newAmountFormat(amountDue)) }
            var card = PaymentAmountCard(amountEntered, paymentMethodsResponse?.paymentMethods, account)
            payMyAccountViewModel.setPMAVendorCard(card)
            when (httpCode) {
                200 -> {
                    payMyAccountViewModel.setPaymentMethodsResponse(this)
                    mPaymentMethodsResponse = paymentMethodsResponse
                    val hasFirstCardExpired = if (paymentMethods?.size ?: 0 > 0) paymentMethods?.get(0)?.cardExpired
                            ?: false else false
                    payUMethodType = when (paymentMethods?.size ?: 0 > 0 || paymentMethods?.isNullOrEmpty() == false || hasFirstCardExpired) {
                        true -> {
                            account = mAvailableFundPresenter?.getAccountDetail()
                            amountEntered = account?.second?.totalAmountDue?.let { amountDue -> Utils.removeNegativeSymbol(WFormatter.newAmountFormat(amountDue)) }
                            card = PaymentAmountCard(amountEntered, mPaymentMethodsResponse?.paymentMethods, account)
                            payMyAccountViewModel.setPMAVendorCard(card)
                            PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE
                        }
                        else -> PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                    }
                }

                400 -> {
                    val code = response.code
                    payUMethodType = when (code.startsWith("P0453")) {
                        true -> {
                            payMyAccountViewModel.setPaymentMethodType(PayMyAccountViewModel.PAYUMethodType.CREATE_USER)
                            PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                        }
                        false -> PayMyAccountViewModel.PAYUMethodType.ERROR

                    }
                }

                440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams
                        ?: "", activity)

                else -> payUMethodType = PayMyAccountViewModel.PAYUMethodType.ERROR
            }
            payMyAccountViewModel.setPaymentMethodType(payUMethodType)
        }
    }

    override fun onPayUMethodFailure(error: Throwable?) {
        payMyAccountViewModel.setPaymentMethodType(PayMyAccountViewModel.PAYUMethodType.ERROR)
        isQueryPayUPaymentMethodComplete = true
        if (error is ConnectException) {
            isQueryPayUPaymentMethodComplete = false
        }
        activity?.runOnUiThread {
            stopProgress()
        }
    }

    fun initShimmer() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        viewPaymentOptionImageShimmerLayout?.setShimmer(shimmer)
        viewPaymentOptionTextShimmerLayout?.setShimmer(shimmer)
    }

    fun startProgress() {
        viewPaymentOptionImageShimmerLayout?.startShimmer()
        viewPaymentOptionTextShimmerLayout?.startShimmer()
    }

    fun stopProgress() {
        viewPaymentOptionImageShimmerLayout?.setShimmer(null)
        viewPaymentOptionImageShimmerLayout?.stopShimmer()

        viewPaymentOptionTextShimmerLayout?.setShimmer(null)
        viewPaymentOptionTextShimmerLayout?.stopShimmer()
    }

    fun navigateToPayMyAccount(payUMethodType: PayMyAccountViewModel.PAYUMethodType, openCardOptionsDialog: () -> Unit) {
        val payMyAccountOption: PayMyAccount? = WoolworthsApplication.getPayMyAccountOption()
        val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
        when {
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CREATE_USER) && isFeatureEnabled -> {
                navigateToPayMyAccountActivity(PayMyAccountStartDestinationType.CREATE_USER)
            }
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> openCardOptionsDialog()

            else -> navigateToPayMyAccountActivity(PayMyAccountStartDestinationType.CREATE_USER)
        }
    }

}
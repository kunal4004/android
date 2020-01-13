package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_statement_button.*
import za.co.woolworths.financial.services.android.contracts.AvailableFundContract
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.util.*

open class AvailableFundFragment : Fragment(), View.OnClickListener, AvailableFundContract.AvailableFundView {
    var mAvailableFundPresenter: AvailableFundPresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAvailableFundPresenter = AvailableFundPresenterImpl(this, AvailableFundModelImpl())
        mAvailableFundPresenter?.setBundle(arguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_available_fund_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        incRecentTransactionButton?.setOnClickListener(this)
    }

    private fun setUpView() {
        mAvailableFundPresenter?.getAccount()?.apply {
            val availableFund =
                    Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(availableFunds), 1, activity))
            val currentBalance =
                    Utils.removeNegativeSymbol(WFormatter.newAmountFormat(currentBalance))
            val creditLimit =
                    Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(creditLimit), 1, activity))
            val paymentDueDate =
                    paymentDueDate?.let { paymentDueDate -> WFormatter.addSpaceToDate(WFormatter.newDateFormat(paymentDueDate)) }
            val totalAmountDueAmount =
                    Utils.removeNegativeSymbol(WFormatter.newAmountFormat(totalAmountDue))
            availableFundAmountTextView?.text = availableFund
            currentBalanceAmountTextView?.text = currentBalance
            creditLimitAmountTextView?.text = creditLimit
            totalAmountDueAmountTextView?.text = totalAmountDueAmount
            nextPaymentDueDateTextView?.text = paymentDueDate
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> navigateToTransactionActivity()
        }
    }

    private fun navigateToTransactionActivity() {
        mAvailableFundPresenter?.getAccount()?.apply {
            (activity as? AccountSignedInActivity)?.let { activity ->
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS)
                val intent = Intent(activity, WTransactionsActivity::class.java)
                intent.putExtra("productOfferingId", productOfferingId.toString())
                intent.putExtra("accountNumber", accountNumber.toString())
                intent.putExtra("cardType", mAvailableFundPresenter?.getCardType())
                activity.startActivityForResult(intent, 0)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    fun navigateToStatementActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS)
            val openStatement = Intent(this, StatementActivity::class.java)
            startActivity(openStatement)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    fun navigateToPaymentOptionActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity -> ScreenManager.presentHowToPayActivity(activity, mAvailableFundPresenter?.getBundle()) }
    }

    override fun navigateToOnlineBankingActivity(creditCardNumber: String, isRegistered: Boolean) {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            val openABSAOnlineBanking =
                    Intent(this, ABSAOnlineBankingRegistrationActivity::class.java)
            openABSAOnlineBanking.putExtra(ABSAOnlineBankingRegistrationActivity.SHOULD_DISPLAY_LOGIN_SCREEN, isRegistered)
            openABSAOnlineBanking.putExtra("creditCardToken", creditCardNumber)
            startActivityForResult(openABSAOnlineBanking, MyAccountCardsActivity.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun displayCardNumberNotFound() {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity -> Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, activity.resources?.getString(R.string.card_number_not_found)) }
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
}
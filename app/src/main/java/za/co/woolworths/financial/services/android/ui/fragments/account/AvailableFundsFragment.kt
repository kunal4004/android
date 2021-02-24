package za.co.woolworths.financial.services.android.ui.fragments.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.util.*

open class AvailableFundsFragment : Fragment(), View.OnClickListener {
    private var mAccountPair: Pair<ApplyNowState, Account>? = null
    private var mAccount: Account? = null

    @Throws(RuntimeException::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE)
                ?: throw RuntimeException("Accounts object is null or not found")
        mAccountPair = Gson().fromJson(account, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
        mAccount = mAccountPair?.second
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_available_fund_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
    }

    @SuppressLint("VisibleForTests")
    private fun setUpView() {
        mAccount?.apply {
            val availableFund = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCent(availableFunds), 1))
            val currentBalance = Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCent(currentBalance))
            val creditLimit = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCent(creditLimit), 1))
            val paymentDueDate = paymentDueDate?.let { paymentDueDate -> WFormatter.addSpaceToDate(WFormatter.newDateFormat(paymentDueDate)) }
            val totalAmountDueAmount = Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCent(totalAmountDue))
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
            R.id.incViewStatementButton -> navigateToStatementActivity()
        }
    }

    private fun navigateToTransactionActivity() {
        (activity as? AccountSignedInActivity)?.let { activity ->
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS)
            val intent = Intent(activity, WTransactionsActivity::class.java)
            intent.putExtra("productOfferingId", mAccount?.productOfferingId?.toString())
            intent.putExtra("accountNumber", mAccount?.accountNumber?.toString())
            intent.putExtra(ACCOUNTS, Gson().toJson(mAccountPair))
            intent.putExtra("cardType", AccountsProductGroupCode.STORE_CARD.groupCode)
            activity.startActivityForResult(intent, 0)
            activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    private fun navigateToStatementActivity() {
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS)
            val openStatement = Intent(this, StatementActivity::class.java)
            openStatement.putExtra(ACCOUNTS, Gson().toJson(mAccountPair))
            startActivity(openStatement)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }
}
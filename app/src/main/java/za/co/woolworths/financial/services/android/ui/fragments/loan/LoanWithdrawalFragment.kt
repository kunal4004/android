package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.loan_withdrawal.*
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.IssueLoan
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import java.util.*
import android.content.Context
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.util.*


class LoanWithdrawalFragment : LoanBaseFragment() {

    private var mMenu: Menu? = null
    private var mPostLoanIssue: Call<IssueLoanResponse>? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var autoPostLoanConnectIsActivated: Boolean = false
    private var MILIS: Long = 200

    companion object {

        const val PERSONAL_LOAN_INFO = "PERSONAL_LOAN_INFO"
        fun newInstance(accountInfo: String?) = LoanWithdrawalFragment().withArgs {
            putString(PERSONAL_LOAN_INFO, accountInfo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.loan_withdrawal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mErrorHandlerView = ErrorHandlerView(activity)
        configureEditText()
        populatePersonalLoanView()
        uniqueIdForPLDDModule()
    }

    private fun uniqueIdForPLDDModule() {
        activity?.resources?.let {
            relLoanWithdrawal?.contentDescription = getString(R.string.loan_withdrawal_drawn_down_amount_layout)
            relWithdrawalAmount?.contentDescription = getString(R.string.enter_drawn_down_amount_layout)
            availableFundsLinearLayout?.contentDescription = getString(R.string.available_funds_layout)
            credit_limit_layout?.contentDescription = getString(R.string.pldd_credit_limit_layout)
        }
    }

    private fun populatePersonalLoanView() {
        val activity = activity ?: return
        tvAvailableFunds.setText(currencyFormatter(getAvailableFund(), activity))
        tvCreditLimit.setText(currencyFormatter(getCreditLimit(), activity))
    }

    private fun configureEditText() {
        edtWithdrawAmount.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtWithdrawAmount.addTextChangedListener(NumberTextWatcherForThousand(edtWithdrawAmount))
        edtWithdrawAmount.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtWithdrawAmount.setRawInputType(Configuration.KEYBOARD_12KEY)
        edtWithdrawAmount.imeOptions = EditorInfo.IME_ACTION_DONE
        edtWithdrawAmount.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                if (arrowIsVisible) confirmDrawnDownAmount()
            }
            handled
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.loan_withdrawal_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.mMenu = menu
        menuItemVisible(menu, false)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.let { finishActivity(it) }
                return true
            }
            R.id.itemNextArrow -> {
                confirmDrawnDownAmount()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDrawnDownAmount(): Int {
        val mCurrentDrawnAmount = amountToInt(edtWithdrawAmount.text.toString())
        return if (TextUtils.isEmpty(mCurrentDrawnAmount)) 0 else mCurrentDrawnAmount.toInt()
    }

    fun menuItemVisible(menu: Menu, isVisible: Boolean) {
        arrowIsVisible = isVisible
        try {
            val menuItem = menu.findItem(R.id.itemNextArrow)
            if (isVisible) {
                menuItem.isEnabled = true
                menuItem.icon.alpha = 255
            } else {
                menuItem.isEnabled = false
                menuItem.icon.alpha = 50
            }
        } catch (ignored: Exception) {
        }
    }

    inner class NumberTextWatcherForThousand(private var edtLoanWithdrawal: EditText) : TextWatcher {
        private var previousLength: Int = 0
        private var backSpace: Boolean = false

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            previousLength = s.length
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            //If s is empty disable next arrow,else enable s arrow
            mMenu?.let { menuItemVisible(it, !TextUtils.isEmpty(s)) }
        }

        @SuppressLint("SetTextI18n")
        override fun afterTextChanged(s: Editable) {
            val initLength: Int = edtLoanWithdrawal.text.length
            val value = edtLoanWithdrawal.text.toString()
            val editLength: Int
            backSpace = previousLength > s.length
            if (value.startsWith("0") && !value.startsWith("0.")) {
                edtLoanWithdrawal.setText("") //Prevents "0" while starting but not "0."
            }
            if (backSpace) {
                edtLoanWithdrawal.removeTextChangedListener(this)
                if (s.isNotEmpty()) {
                    var loanAmount = s.toString()
                            .replace(".0", "")
                            .replace(" ", "")
                    loanAmount = loanAmount.substring(0, loanAmount.length - 1)
                    if (TextUtils.isEmpty(loanAmount)) {
                        edtLoanWithdrawal.setText("")
                        backSpace = false
                        mMenu?.let { menuItemVisible(it, false) }
                        edtLoanWithdrawal.addTextChangedListener(this)
                        return
                    }
                    val cp = edtLoanWithdrawal.selectionStart
                    loanAmount = getDecimalFormat(trimCommaOfString(loanAmount)) + ".00"
                    edtLoanWithdrawal.setText(loanAmount)
                    editLength = edtLoanWithdrawal.text.length
                    val sel = cp + (editLength - initLength)
                    if (sel > 0) {
                        edtLoanWithdrawal.setSelection(editLength)
                    }
                }
                edtLoanWithdrawal.addTextChangedListener(this)
            } else {
                try {
                    edtLoanWithdrawal.removeTextChangedListener(this)
                    val value = edtLoanWithdrawal.text.toString()

                    if (value != "") {

                        if (value.startsWith(".")) { //adds "0." when only "." is pressed on beginning of writing
                            edtLoanWithdrawal.setText("0.")
                        }
                        if (value.startsWith("0") && !value.startsWith("0.")) {
                            edtLoanWithdrawal.setText("") //Prevents "0" while starting but not "0."
                        }

                        val str = edtLoanWithdrawal.text.toString().replace(" ".toRegex(), "")
                        if (value != "")
                            edtLoanWithdrawal.setText(getDecimalFormat(str) + ".00")
                        edtLoanWithdrawal.setSelection(edtLoanWithdrawal.text.toString().length)
                    }
                    edtLoanWithdrawal.addTextChangedListener(this)
                } catch (ex: Exception) {
                    edtLoanWithdrawal.addTextChangedListener(this)
                }
            }
        }

        private fun getDecimalFormat(number: String): String {
            var value = number
            value = value.replace(".00", "")
            val lst = StringTokenizer(value, ".")
            var str1 = value
            var str2 = ""
            if (lst.countTokens() > 1) {
                str1 = lst.nextToken()
                str2 = lst.nextToken()
            }
            var str3 = ""
            var i = 0
            var j = -1 + str1.length
            if (str1[-1 + str1.length] == '.') {
                j--
                str3 = "."
            }
            var k = j
            while (true) {
                if (k < 0) {
                    if (str2.isNotEmpty())
                        str3 = "$str3.$str2"
                    return str3
                }
                if (i == 3) {
                    str3 = " $str3"
                    i = 0
                }
                str3 = str1[k] + str3
                i++
                k--
            }

        }

        //Trims all the comma of the string and returns
        private fun trimCommaOfString(string: String): String {
            //        String returnString;
            return if (string.contains(" ")) {
                string.replace(" ", "")
            } else {
                string
            }

        }
    }

    private fun confirmDrawnDownAmount() {
        if (getDrawnDownAmount() < getMinDrawnAmountWithoutCent()) {
            Utils.displayValidationMessage(activity,
                    CustomPopUpWindow.MODAL_LAYOUT.LOW_LOAN_AMOUNT,
                    getDrawnDownAmount().toString())
        } else if (getDrawnDownAmount() >= getMinDrawnAmountWithoutCent() && getDrawnDownAmount() <= getAvailableFundWithoutCent()) {
            val productOfferingId = getProductOfferingId()
            val drawnDownAmountInCent = getDrawnDownAmount() * 100
            val creditLimit = getCreditLimit()
            val issueLoanRequest = IssueLoan(productOfferingId,
                    drawnDownAmountInCent, repaymentPeriod(drawnDownAmountInCent), creditLimit)
            showProgressDialog(true)

            mPostLoanIssue =  OneAppService.issueLoan(issueLoanRequest)
            mPostLoanIssue?.enqueue(CompletionHandler(object: RequestListener<IssueLoanResponse>{
                override fun onSuccess(issueLoanResponse: IssueLoanResponse?) {
                    activity?.let { activity ->
                        issueLoanResponse?.apply {
                            autoConnectRequest(false)
                            showProgressDialog(false)
                            hideKeyboard()
                            when (httpCode) {
                                200 -> {
                                    replaceFragment(
                                            fragment = LoanWithdrawalDetailFragment.newInstance(Utils.toJson(issueLoanRequest), installmentAmount),
                                            tag = LoanWithdrawalDetailFragment::class.java.simpleName,
                                            containerViewId = R.id.flLoanContent,
                                            allowStateLoss = true,
                                            enterAnimation = R.anim.slide_in_from_right,
                                            exitAnimation = R.anim.slide_to_left,
                                            popEnterAnimation = R.anim.slide_from_left,
                                            popExitAnimation = R.anim.slide_to_right
                                    )
                                }
                                440 -> {
                                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams, activity)
                                }

                                else -> {
                                    response?.desc?.let { DialogManager(activity).showBasicDialog(it) }
                                }
                            }
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    activity?.apply {
                        autoConnectRequest(true)
                        showProgressDialog(false)
                    }
                }

            },IssueLoanResponse::class.java))

        } else {
            activity?.let {  Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.HIGH_LOAN_AMOUNT, "")}
        }
    }

    private fun autoConnectRequest(isConnected: Boolean) {
        autoPostLoanConnectIsActivated = isConnected
    }

    private fun showProgressDialog(isVisible: Boolean) {
        mLoanWithdrawalProgress?.visibility = if (isVisible) VISIBLE else GONE
        llDrawndownAmount?.visibility = if (isVisible) GONE else VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.apply {
            runOnUiThread {
                hideKeyboard()
            }
        }
        mPostLoanIssue?.let {
            if (!it.isCanceled)
                it.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        edtWithdrawAmount?.apply {
            val drawnDownAmount: String = text.toString()
            // retrieve drawnDown amount
            // check if drawnDownAmount is empty
            val drawnDownAmountIsEmpty = TextUtils.isEmpty(drawnDownAmount)
            // Change icon to close icon
            activity?.let { (it as? LoanWithdrawalActivity)?.setHomeIndicatorIcon(R.drawable.close_white) }
            if (!drawnDownAmountIsEmpty)
                setText(drawnDownAmount)
            Handler().postDelayed({ mMenu?.let { menuItemVisible(it, !drawnDownAmountIsEmpty) } }, MILIS)
            showKeyboard()
        }
    }

    private fun hideKeyboard() {
        activity?.let {
            it.currentFocus?.windowToken?.apply {
                (it.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(this, 0)
            }
        }
    }

    private fun showKeyboard() {
        edtWithdrawAmount?.apply {
            requestFocus()
            activity?.let {
                requestFocus()
                isFocusableInTouchMode = true
                val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    fun onConnectionChanged(hasInternet: Boolean) {
        activity?.runOnUiThread {
            if (hasInternet) {
                if (arrowIsVisible && autoPostLoanConnectIsActivated) {
                    showProgressDialog(true)
                    confirmDrawnDownAmount()
                }
            } else {
                showProgressDialog(false)
                mErrorHandlerView?.showToast()
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.View.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.enter_payment_amount_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAYMENT_DETAIL_CARD_UPDATE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.IS_DONE_BUTTON_ENABLED
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.getFuturaMediumFont
import za.co.woolworths.financial.services.android.util.CurrencySymbols
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class EnterPaymentAmountFragment : Fragment(), OnClickListener {

    private var isDoneButtonEnabled: Boolean = false
    private var isAmountSelected = false // Prevent cursor to jump to front when re-selecting same amount
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            isDoneButtonEnabled = getBoolean(IS_DONE_BUTTON_ENABLED, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enter_payment_amount_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar()
        configureButton()
        configureCurrencyEditText()
        setListeners()

        paymentAmountInputEditText?.requestFocus()

        with(payMyAccountViewModel) {
            totalAmountDueValueTextView?.text = getTotalAmountDue()
            if (isAccountChargedOff()) {
                amountOverdueLabelTextView?.text = getString(R.string.current_balance_label)
                amountOutstandingValueTextView?.text = getCurrentBalance()
            } else {
                amountOverdueLabelTextView?.text = getString(R.string.overdue_amount_label)
                amountOutstandingValueTextView?.text = getOverdueAmount()
            }
            paymentAmountInputEditText?.setText(getAmountEntered())
        }
    }

    private fun setListeners() {
        totalAmountDueValueTextView?.apply {
            if (isZeroAmount(payMyAccountViewModel.getTotalAmountDue())) return
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@EnterPaymentAmountFragment)
        }

        amountOutstandingValueTextView?.apply {
            if (payMyAccountViewModel.isAccountChargedOff()) {
                if (isZeroAmount(payMyAccountViewModel.getCurrentBalance())) return
            } else {
                if (isZeroAmount(payMyAccountViewModel.getOverdueAmount())) return
            }
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@EnterPaymentAmountFragment)
        }
    }

    private fun configureToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            displayToolbarDivider(false)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun configureButton() {
        continueToPaymentButton?.apply {
            text = if (isDoneButtonEnabled) bindString(R.string.done) else bindString(R.string.confirm_payment)
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@EnterPaymentAmountFragment)
        }
        continueToPaymentButton?.isEnabled = false
    }

    private fun configureCurrencyEditText() {
        paymentAmountInputEditText?.apply {

            setCurrencySymbol(CurrencySymbols.NONE)
            setDelimiter(false)
            setSpacing(true)
            setDecimals(true)
            setSeparator(".")

            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
            transformationMethod = null
            typeface = getFuturaMediumFont()

            addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    continueToPaymentButton?.isEnabled = s.isNotEmpty()
                    when (this@apply.text?.toString()) {
                        payMyAccountViewModel.getOverdueAmount() -> {
                            when (enterPaymentAmountTextView?.tag) {
                                R.id.totalAmountDueValueTextView -> selectTotalAmountDue()
                                else -> selectOutstandingAmount()
                            }
                        }
                        payMyAccountViewModel.getTotalAmountDue() -> selectTotalAmountDue()
                        else -> clearSelection()
                    }

                    if (isAmountSelected && !TextUtils.isEmpty(s)) {
                        setSelection(s.length)
                        isAmountSelected = false
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    reducePaymentAmountTextView?.visibility = GONE
                }
            })

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continueToPaymentButton -> {
                with(payMyAccountViewModel) {
                    val inputValue = paymentAmountInputEditText?.text?.toString()

                    validateAmountEntered(convertRandFormatToDouble(inputValue), {
                        // Minimum amount excess error
                        enteredAmountValidationError(bindString(R.string.enter_payment_amount_min_input_error))
                    }, {
                        // Maximum amount excess error
                        enteredAmountValidationError(bindString(R.string.enter_payment_amount_max_input_error))
                    }, {
                        // Amount is valid
                        val cardInfo = getCardDetail()
                        cardInfo?.amountEntered = inputValue
                        setPMACardInfo(cardInfo)

                        switchToConfirmPaymentOrDoneButton(continueToPaymentButton?.text?.toString(), {
                            //Done button
                            hideKeyboard()
                            activity?.apply {
                                setResult(RESULT_OK, Intent().putExtra(PAYMENT_DETAIL_CARD_UPDATE, Gson().toJson(cardInfo)))
                                finish()
                            }
                        }, {
                            //Confirm Payment button
                            hideKeyboard()
                            view?.apply {
                                Navigation.findNavController(this)
                                        .navigate(R.id.action_enterPaymentAmountFragment_to_addNewPayUCardFragment)
                            }
                        })
                    })
                }
            }

            R.id.totalAmountDueValueTextView -> {
                if (isZeroAmount(payMyAccountViewModel.getTotalAmountDue())) return
                enterPaymentAmountTextView?.tag = R.id.totalAmountDueValueTextView
                isAmountSelected = true
                selectTotalAmountDue()
                paymentAmountInputEditText?.setText(payMyAccountViewModel.getTotalAmountDue())
            }

            R.id.amountOutstandingValueTextView -> {
                if (payMyAccountViewModel.isAccountChargedOff()) {
                    if (isZeroAmount(payMyAccountViewModel.getCurrentBalance())) return
                    selectCurrentBalance()
                    paymentAmountInputEditText?.setText(payMyAccountViewModel.getCurrentBalance())
                } else {
                    if (isZeroAmount(payMyAccountViewModel.getOverdueAmount())) return
                    selectOutstandingAmount()
                    paymentAmountInputEditText?.setText(payMyAccountViewModel.getOverdueAmount())
                }
                enterPaymentAmountTextView?.tag = R.id.amountOutstandingValueTextView
                isAmountSelected = true

            }
        }
    }

    private fun selectOutstandingAmount() {
        when (isZeroAmount(payMyAccountViewModel.getOverdueAmount())) {
            true -> clearSelection()
            else -> {
                totalAmountDueValueTextView?.isSelected = false
                amountOutstandingValueTextView?.isSelected = true
            }
        }
    }

    private fun selectCurrentBalance() {
        when (isZeroAmount(payMyAccountViewModel.getCurrentBalance())) {
            true -> clearSelection()
            else -> {
                totalAmountDueValueTextView?.isSelected = false
                amountOutstandingValueTextView?.isSelected = true
            }
        }
    }

    private fun selectTotalAmountDue() {
        when (isZeroAmount(payMyAccountViewModel.getTotalAmountDue())) {
            true -> clearSelection()
            else -> {
                totalAmountDueValueTextView?.isSelected = true
                amountOutstandingValueTextView?.isSelected = false
            }
        }
    }

    private fun isZeroAmount(amount: String?) = payMyAccountViewModel.convertRandFormatToInt(amount) == 0

    private fun clearSelection() {
        totalAmountDueValueTextView?.isSelected = false
        amountOutstandingValueTextView?.isSelected = false
    }

    private fun enteredAmountValidationError(amount: String) {
        continueToPaymentButton?.isEnabled = false
        reducePaymentAmountTextView?.visibility = VISIBLE
        reducePaymentAmountTextView?.text = amount
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                hideKeyboard()
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? AppCompatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
    }

    fun showKeyboard() {
        paymentAmountInputEditText?.requestFocus()
        val imm: InputMethodManager? = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun hideKeyboard() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }
}
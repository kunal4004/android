package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.*
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
import za.co.woolworths.financial.services.android.util.CurrencySymbols
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class EnterPaymentAmountFragment : Fragment(), OnClickListener {

    private var isDoneButtonEnabled: Boolean = false

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

        with(payMyAccountViewModel) {
            totalAmountDueValueTextView?.text = getOverdueAmount()
            amountOutstandingValueTextView?.text = getTotalAmountDue()
            paymentAmountInputEditText?.setText(getAmountEntered())
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

            setCurrency(CurrencySymbols.NONE)
            setDelimiter(false)
            setSpacing(true)
            setDecimals(true)
            setSeparator(".")

            addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    with(payMyAccountViewModel) {
                        continueToPaymentButton?.isEnabled = s.isNotEmpty()
                        val paymentAmount = getAmountEnteredAfterTextChanged(s.toString())
                        amountOutstandingValueTextView?.text = paymentAmount
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    reducePaymentAmountTextView?.visibility = INVISIBLE
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
                            activity?.apply {
                                setResult(RESULT_OK, Intent().putExtra(PAYMENT_DETAIL_CARD_UPDATE, Gson().toJson(cardInfo)))
                                finish()
                            }
                        }, {
                            //Confirm Payment button
                            view?.apply {
                                Navigation.findNavController(this)
                                        .navigate(R.id.action_enterPaymentAmountFragment_to_addNewPayUCardFragment)
                            }
                        })
                    })
                }
            }
        }
    }

    private fun enteredAmountValidationError(amount: String) {
        continueToPaymentButton?.isEnabled = false
        reducePaymentAmountTextView?.visibility = VISIBLE
        reducePaymentAmountTextView?.text = amount
    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
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
        hideKeyboard()
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
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
}
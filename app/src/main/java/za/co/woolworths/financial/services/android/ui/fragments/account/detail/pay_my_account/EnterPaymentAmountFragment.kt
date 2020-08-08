package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_payment_amount_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.text.NumberFormat
import java.util.*

class EnterPaymentAmountFragment : Fragment(), View.OnClickListener {

    private var account: Account? = null

    private var navController: NavController? = null

    val args: EnterPaymentAmountFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enter_payment_amount_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        account = args.account

        configureToolbar()
        configureButton()
        configureCurrencyEditText()

        totalAmountDueValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.totalAmountDue
                ?: 0))
        amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.amountOverdue
                ?: 0))
    }

    private fun configureToolbar() {
        (activity as? AppCompatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun configureButton() {
        continueToPaymentButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@EnterPaymentAmountFragment)
        }
        continueToPaymentButton?.isEnabled = false
    }

    private fun configureCurrencyEditText() {


        paymentAmountInputEditText?.apply {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL

            var current = ""

            addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    continueToPaymentButton?.isEnabled = s.isNotEmpty()
                    val enteredAmount = paymentAmountInputEditText?.text?.toString()?.replace("[,.R ]".toRegex(), "")?.toInt()?.let { inputAmount -> account?.amountOverdue?.minus(inputAmount * 100) } ?: 0
                    amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(enteredAmount))
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        val stringText = s.toString()

                        if (stringText != current || stringText.isNotEmpty()) {
                            removeTextChangedListener(this)

                            val locale: Locale = Locale.US
                            val currency = Currency.getInstance(locale)
                            val cleanString = stringText.replace("[${currency.symbol},.R ]".toRegex(), "")
                            val parsed = cleanString.toDouble()
                            val formatted = NumberFormat.getCurrencyInstance(locale).format(parsed / 100)

                            current = formatted.replace("[,]".toRegex(), " ").replace("[$]".toRegex(), "")
                            setText(current)
                            setSelection(current.length)
                            addTextChangedListener(this)
                        }
                    } catch (ex: Exception) {
                        Log.e("Exception", ex.message)
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                GlobalScope.doAfterDelay(100) {
                    activity?.onBackPressed()
                }
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continueToPaymentButton -> {
                navController?.navigate(R.id.action_enterPaymentAmountFragment_to_addNewPayUCardFragment)
                hideKeyboard()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }

    private fun showKeyboard() {
        paymentAmountInputEditText?.requestFocus()
        val imm: InputMethodManager? = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}
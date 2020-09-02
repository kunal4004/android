package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.enter_payment_amount_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.IS_DONE_BUTTON_ENABLED
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.CurrencySymbols
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.lang.Exception

class EnterPaymentAmountFragment : Fragment(), OnClickListener {

    private var mPaymentMethod: MutableList<GetPaymentMethod>? = null
    private var paymentMethod: String? = null
    private var accountInfo: String? = null
    private var account: Account? = null
    private var navController: NavController? = null
    private var isDoneButtonEnabled: Boolean = false

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    val args: EnterPaymentAmountFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            accountInfo = getString(PayMyAccountPresenterImpl.GET_ACCOUNT_INFO, "")
            paymentMethod = getString(PayMyAccountPresenterImpl.GET_PAYMENT_METHOD, "")
            isDoneButtonEnabled = getBoolean(IS_DONE_BUTTON_ENABLED, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enter_payment_amount_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // TODO:: R&D and implement, pass data from activity to fragment with safeArgs directions
        try {
            account = args.account
        } catch (e: Exception) {
            val cardInfo = payMyAccountViewModel.getCardDetail()
            account = cardInfo?.account?.second
            mPaymentMethod = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethod, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
        }

        configureToolbar()
        configureButton()
        configureCurrencyEditText()

        totalAmountDueValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.totalAmountDue ?: 0))
        amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.amountOverdue ?: 0))
        paymentAmountInputEditText?.setText(payMyAccountViewModel.getCardDetail()?.amountEntered)

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
                    continueToPaymentButton?.isEnabled = s.isNotEmpty()
                    var paymentAmount = paymentAmountInputEditText?.text?.toString()?.replace("[,.R ]".toRegex(), "")
                    if (TextUtils.isEmpty(paymentAmount)){
                        paymentAmount = "0"
                    }
                    var enteredAmount = paymentAmount?.toInt()?.let { inputAmount -> account?.amountOverdue?.minus(inputAmount) } ?: 0
                    enteredAmount = if (enteredAmount < 0) 0 else enteredAmount
                    amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(enteredAmount))

                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    reducePaymentAmountTextView?.visibility = INVISIBLE
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continueToPaymentButton -> {
                val amountEntered = paymentAmountInputEditText?.text?.toString()
                val enteredAmount = amountEntered?.replace("[R ]".toRegex(), "")?.toDouble() ?: 0.0
                when {
                    enteredAmount < 1.toDouble() -> {
                        continueToPaymentButton?.isEnabled = false
                        reducePaymentAmountTextView?.visibility = VISIBLE
                        reducePaymentAmountTextView?.text = bindString(R.string.enter_payment_amount_min_input_error)
                        return
                    }
                    enteredAmount > 50000.toDouble() -> {
                        continueToPaymentButton?.isEnabled = false
                        reducePaymentAmountTextView?.visibility = VISIBLE
                        reducePaymentAmountTextView?.text = bindString(R.string.enter_payment_amount_max_input_error)
                        return
                    }
                }

                val selectedCard = payMyAccountViewModel.getCardDetail()
                selectedCard?.amountEntered = amountEntered
                payMyAccountViewModel.setPMAVendorCard(selectedCard)

                if (continueToPaymentButton?.text?.toString() == bindString(R.string.done)) {

                    activity?.apply {
                        if (isDoneButtonEnabled) {
                            setResult(RESULT_OK, Intent().putExtra("AMOUNT_ENTERED", Gson().toJson(selectedCard)))
                            finish()
                        } else {
                            activity?.onBackPressed()
                        }
                    }
                    return
                }

                navController?.navigate(R.id.action_enterPaymentAmountFragment_to_addNewPayUCardFragment)
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
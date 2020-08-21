package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.enter_payment_amount_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.CurrencySymbols
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.lang.Exception

class EnterPaymentAmountFragment : Fragment(), View.OnClickListener {

    private var mPaymentMethod: MutableList<GetPaymentMethod>? = null
    private var paymentMethod: String? = null
    private var accountInfo: String? = null
    private var account: Account? = null

    private var navController: NavController? = null

    val args: EnterPaymentAmountFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            accountInfo = getString(PayMyAccountPresenterImpl.ACCOUNT_INFO, "")
            paymentMethod = getString(PayMyAccountPresenterImpl.PAYMENT_METHOD, "")
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
            account = Gson().fromJson(accountInfo, Account::class.java)
            mPaymentMethod = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethod, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
        }

        configureToolbar()
        configureButton()
        configureCurrencyEditText()

        totalAmountDueValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.totalAmountDue
                ?: 0))
        amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.amountOverdue
                ?: 0))
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
                    var enteredAmount = paymentAmountInputEditText?.text?.toString()?.replace("[,.R ]".toRegex(), "")?.toInt()?.let { inputAmount -> account?.amountOverdue?.minus(inputAmount) } ?: 0
//                    when {
//                        enteredAmount < 1 -> {
//                            continueToPaymentButton?.isEnabled = false
//                            reducePaymentAmountTextView?.visibility = VISIBLE
//                            reducePaymentAmountTextView?.text = bindString(R.string.enter_payment_amount_min_input_error)
//                        }
//                        enteredAmount > 5000000 -> {
//                            continueToPaymentButton?.isEnabled = false
//                            reducePaymentAmountTextView?.visibility = VISIBLE
//                            reducePaymentAmountTextView?.text = bindString(R.string.enter_payment_amount_max_input_error)
//                        }
//                        else -> {
//                            continueToPaymentButton?.isEnabled = true
//                            reducePaymentAmountTextView?.visibility = GONE
//                        }
//                    }
                    enteredAmount = if (enteredAmount < 0) 0 else enteredAmount
                    amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(enteredAmount))
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continueToPaymentButton -> {
                val amountEntered = paymentAmountInputEditText?.text?.toString()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("amountEntered", amountEntered)
                (activity as? PayMyAccountActivity)?.amountEntered = amountEntered?.replace("[,.R ]".toRegex(), "")?.toInt()!!
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
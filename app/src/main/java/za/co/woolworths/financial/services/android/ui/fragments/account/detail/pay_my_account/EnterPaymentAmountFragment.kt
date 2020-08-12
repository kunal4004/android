package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_payment_amount_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.util.CurrencyInputWatcher
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        account = args.account

        configureToolbar()
        configureButton()
        configureCurrencyEditText()

        totalAmountDueValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.totalAmountDue ?: 0))
        amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account?.amountOverdue ?: 0))
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun configureCurrencyEditText() {
        paymentAmountInputEditText?.apply {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL

            addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    continueToPaymentButton?.isEnabled = s.isNotEmpty()
//                    if (s.isEmpty()) return
//                    val enteredAmount = paymentAmountInputEditText?.text?.toString()?.replace("[,.R ]".toRegex(), "")?.toInt()?.let { inputAmount -> account?.amountOverdue?.minus(inputAmount * 100) } ?: 0
//                    amountOutstandingValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(enteredAmount))
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            addTextChangedListener(CurrencyInputWatcher(this,"R ", Locale.getDefault()))
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
                (activity as? PayMyAccountActivity)?.amountEntered = paymentAmountInputEditText?.text?.toString()?.replace("[,.R ]".toRegex(), "")?.toInt()
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
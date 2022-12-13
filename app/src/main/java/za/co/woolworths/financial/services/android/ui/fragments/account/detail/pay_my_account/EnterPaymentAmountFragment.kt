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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.EnterPaymentAmountFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAYMENT_DETAIL_CARD_UPDATE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.IS_DONE_BUTTON_ENABLED
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.getFuturaMediumFont
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel.Companion.DEFAULT_RAND_CURRENCY
import za.co.woolworths.financial.services.android.ui.views.actionsheet.InfoDialogFragment
import za.co.woolworths.financial.services.android.util.CurrencySymbols
import za.co.woolworths.financial.services.android.util.KeyboardUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class EnterPaymentAmountFragment : BaseFragmentBinding<EnterPaymentAmountFragmentBinding>(EnterPaymentAmountFragmentBinding::inflate), OnClickListener {

    private var isDoneButtonEnabled: Boolean = false
    private var isAmountSelected =
        false // Prevent cursor to jump to front when re-selecting same amount
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            isDoneButtonEnabled = getBoolean(IS_DONE_BUTTON_ENABLED, false)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolbar()

        binding.apply {
            configureButton()
            configureCurrencyEditText()
            setListeners()

            paymentAmountInputEditText?.requestFocus()

            with(payMyAccountViewModel) {
                totalAmountDueValueTextView?.text = getTotalAmountDue()
                paymentAmountInputEditText?.setText(getAmountEntered())
                if (isAccountChargedOff()) {
                    if (elitePlanModel?.scope.isNullOrEmpty()) {
                        amountOverdueLabelTextView?.text = getString(R.string.current_balance_label)
                        amountOutstandingValueTextView?.text = getCurrentBalance()
                    } else {
                        setViewsForElitePlan(this)
                    }
                } else {
                    amountOverdueLabelTextView?.text = getString(R.string.overdue_amount_label)
                    amountOutstandingValueTextView?.text = getOverdueAmount()
                }
            }
        }

        setFragmentResultListener(InfoDialogFragment::class.java.simpleName) { _, _ ->
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            showKeyboard()
        }
    }

    private fun EnterPaymentAmountFragmentBinding.setViewsForElitePlan(payMyAccountViewModel: PayMyAccountViewModel) {
        amountOverdueLabelTextView?.text = getString(R.string.overdue_amount_label)
        enterPaymentAmountTextView?.text = getString(R.string.amount_payable)
        amountOutstandingValueTextView?.text = payMyAccountViewModel.getDiscountAmount()
        paymentAmountInputEditText?.setText(payMyAccountViewModel.getDiscountAmount())
        paymentAmountInputEditText?.isEnabled = false
        amountYouSaveValueTextView?.text = payMyAccountViewModel.getSavedAmount()
        totalAmountGroup?.visibility = GONE
        amountOutstandingValueTextView.isActivated = true
        amountOutstandingValueTextView.isClickable = false
        amountYouSaveGroup.visibility = VISIBLE
    }

    private fun EnterPaymentAmountFragmentBinding.setListeners() {
        currentBalanceDescImageButton?.setOnClickListener(this@EnterPaymentAmountFragment)
        totalAmountDueInfoDescImageButton?.setOnClickListener(this@EnterPaymentAmountFragment)
        amountYouSaveImageButton?.setOnClickListener(this@EnterPaymentAmountFragment)

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

    private fun EnterPaymentAmountFragmentBinding.configureButton() {
        continueToPaymentButton?.apply {
            text =
                if (isDoneButtonEnabled) bindString(R.string.done) else bindString(R.string.continue_to_payment)
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@EnterPaymentAmountFragment)
        }
        continueToPaymentButton?.isEnabled = false
    }

    private fun EnterPaymentAmountFragmentBinding.configureCurrencyEditText() {
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

                override fun afterTextChanged(editableText: Editable) {
                    continueToPaymentButton?.isEnabled =
                        editableText.isNotEmpty() && !editableText.toString()
                            .equals(DEFAULT_RAND_CURRENCY, ignoreCase = true)
                    when (this@apply.text?.toString()) {
                        payMyAccountViewModel.getCurrentBalance() -> selectCurrentBalance()
                        payMyAccountViewModel.getOverdueAmount() -> selectOutstandingAmount()
                        payMyAccountViewModel.getTotalAmountDue() -> selectTotalAmountDue()
                        else -> clearSelection()
                    }

                    if (isAmountSelected && !TextUtils.isEmpty(editableText)) {
                        setSelection(editableText.length)
                        isAmountSelected = false
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    reducePaymentAmountTextView?.visibility = GONE
                    highlightAmountBlock()
                }
            })

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continueToPaymentButton -> {
                with(payMyAccountViewModel) {
                    val inputValue = binding.paymentAmountInputEditText?.text?.toString()

                    validateAmountEntered(convertRandFormatToDouble(inputValue), {
                        // Minimum amount excess error
                        binding.enteredAmountValidationError(bindString(R.string.enter_payment_amount_min_input_error))
                    }, {
                        // Maximum amount excess error
                        binding.enteredAmountValidationError(bindString(R.string.enter_payment_amount_max_input_error))
                    }, {
                        // Amount is valid
                        val cardInfo = getCardDetail()
                        cardInfo?.amountEntered = inputValue
                        setPMACardInfo(cardInfo)

                        switchToConfirmPaymentOrDoneButton(
                            binding.continueToPaymentButton?.text?.toString(),
                            {
                                //Done button
                                hideKeyboard()
                                activity?.apply {
                                    setResult(
                                        RESULT_OK,
                                        Intent().putExtra(
                                            PAYMENT_DETAIL_CARD_UPDATE,
                                            Gson().toJson(cardInfo)
                                        )
                                    )
                                    finish()
                                }
                            },
                            {
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
                binding.enterPaymentAmountTextView?.tag = R.id.totalAmountDueValueTextView
                isAmountSelected = true
                binding.selectTotalAmountDue()
                binding.paymentAmountInputEditText?.setText(payMyAccountViewModel.getTotalAmountDue())
            }

            R.id.amountOutstandingValueTextView -> {
                if (payMyAccountViewModel.isAccountChargedOff()) {
                    if (isZeroAmount(payMyAccountViewModel.getCurrentBalance())) return
                    binding.selectCurrentBalance()
                    binding.paymentAmountInputEditText?.setText(payMyAccountViewModel.getCurrentBalance())
                } else {
                    if (isZeroAmount(payMyAccountViewModel.getOverdueAmount())) return
                    binding.selectOutstandingAmount()
                    binding.paymentAmountInputEditText?.setText(payMyAccountViewModel.getOverdueAmount())
                }
                binding.enterPaymentAmountTextView?.tag = R.id.amountOutstandingValueTextView
                isAmountSelected = true

            }

            R.id.totalAmountDueInfoDescImageButton -> {
                hideKeyboard()
                view?.findNavController()?.navigate(
                    EnterPaymentAmountFragmentDirections.actionEnterPaymentAmountFragmentToInfoDialogFragment(
                        R.string.total_amount_due,
                        R.string.pma_total_amount_due_popup_desc
                    )
                )

            }
            R.id.amountYouSaveImageButton -> {
                hideKeyboard()
                view?.findNavController()?.navigate(
                    EnterPaymentAmountFragmentDirections.actionEnterPaymentAmountFragmentToInfoDialogFragment(
                        R.string.amount_you_save,
                        R.string.total_amount_you_saving
                    )
                )

            }
            R.id.currentBalanceDescImageButton -> {
                hideKeyboard()
                view?.findNavController()?.navigate(
                    if (payMyAccountViewModel.isAccountChargedOff()) {
                        EnterPaymentAmountFragmentDirections.actionEnterPaymentAmountFragmentToInfoDialogFragment(
                            R.string.current_balance_label,
                            R.string.collection_remove_block_current_balance_popup_desc
                        )
                    } else {
                        EnterPaymentAmountFragmentDirections.actionEnterPaymentAmountFragmentToInfoDialogFragment(
                            R.string.overdue_amount_label,
                            R.string.pma_amount_overdue_popup_desc
                        )
                    }
                )
            }
        }
    }

    private fun EnterPaymentAmountFragmentBinding.selectOutstandingAmount() {
        when (isZeroAmount(payMyAccountViewModel.getOverdueAmount())) {
            true -> clearSelection()
            else -> {
                amountOutstandingValueTextView?.isSelected = true
                totalAmountDueValueTextView?.isSelected = false
            }
        }
    }

    private fun EnterPaymentAmountFragmentBinding.selectCurrentBalance() {
        when (isZeroAmount(payMyAccountViewModel.getCurrentBalance())) {
            true -> clearSelection()
            else -> {
                amountOutstandingValueTextView?.isSelected = true
                totalAmountDueValueTextView?.isSelected = false
            }
        }
    }

    private fun EnterPaymentAmountFragmentBinding.selectTotalAmountDue() {
        when (isZeroAmount(payMyAccountViewModel.getTotalAmountDue())) {
            true -> clearSelection()
            else -> {
                totalAmountDueValueTextView?.isSelected = true
                amountOutstandingValueTextView?.isSelected = false
            }
        }
    }

    private fun isZeroAmount(amount: String?) =
        payMyAccountViewModel.convertRandFormatToInt(amount) == 0

    private fun EnterPaymentAmountFragmentBinding.clearSelection() {
        totalAmountDueValueTextView?.isSelected = false
        amountOutstandingValueTextView?.isSelected = false
    }

    private fun EnterPaymentAmountFragmentBinding.enteredAmountValidationError(amount: String) {
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
        // hide keyboard for elite plans
        if (payMyAccountViewModel.elitePlanModel?.scope.isNullOrEmpty()) {
            binding.paymentAmountInputEditText?.requestFocus()
            val imm: InputMethodManager? =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    fun hideKeyboard() {
        activity?.apply {
            if (KeyboardUtils.isSystemKeyboardVisible(this)) {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }

    fun EnterPaymentAmountFragmentBinding.highlightAmountBlock() {
        val inputFieldAmount =
            payMyAccountViewModel.convertRandFormatToDouble(paymentAmountInputEditText?.text?.toString())
        val totalAmountDue = totalAmountDueValueTextView?.text?.toString()
        val overdueAmount = amountOutstandingValueTextView?.text?.toString()
        when (inputFieldAmount.toString()) {
            totalAmountDue -> {
                amountOutstandingValueTextView?.isActivated = false
                totalAmountDueValueTextView?.isActivated = true
            }
            overdueAmount -> { // logic applies to currentBalance as they share amountOutstandingValueTextView
                amountOutstandingValueTextView?.isActivated = true
                totalAmountDueValueTextView?.isActivated = false
            }
            else -> {
                amountOutstandingValueTextView?.isActivated = false
                totalAmountDueValueTextView?.isActivated = false
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.SupplyExpenseDetailFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.SupplyInfoDetailFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.SupplyInfoDetailFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.MultiClickPreventer
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.CLIFragment
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController

class SupplyExpensesDetailFragment : CLIFragment(R.layout.supply_expense_detail_fragment), View.OnClickListener, OnFocusChangeListener {

    private lateinit var binding: SupplyExpenseDetailFragmentBinding

    private var mHashIncomeDetail: HashMap<String, String>? = null
    private var mHashExpenseDetail: HashMap<String, String>? = null
    private var etMortgagePaymentsWasEdited = false
    private var etRentalPaymentsWasEdited = false
    private var etMaintainanceExpensesWasEdited = false
    private var etMonthlyCreditPaymentsWasEdited = false
    private var etOtherExpensesWasEdited = false
    private var etOtherExpensesWasTouched = false
    private var mIncreaseLimitController: IncreaseLimitController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SupplyExpenseDetailFragmentBinding.bind(view)

        binding.apply {
            mIncreaseLimitController = IncreaseLimitController(activity)
            arguments?.let {
                if (it.getSerializable(IncreaseLimitController.INCOME_DETAILS) != null) {
                    mHashIncomeDetail =
                        it.getSerializable(IncreaseLimitController.INCOME_DETAILS) as HashMap<String, String>?
                    mHashExpenseDetail =
                        it.getSerializable(IncreaseLimitController.EXPENSE_DETAILS) as HashMap<String, String>?
                }
            }

            init()
            nextFocusEditText()
            mCliStepIndicatorListener?.onStepSelected(2)
            mHashExpenseDetail?.let { mHashExpenseDetail ->
                mIncreaseLimitController?.populateExpenseField(
                    etMortgagePayments,
                    mHashExpenseDetail["MORTGAGE_PAYMENTS"],
                    tvMortgagePayments
                )
                mIncreaseLimitController?.populateExpenseField(
                    etRentalPayments,
                    mHashExpenseDetail["RENTAL_PAYMENTS"],
                    tvRentalPayments
                )
                mIncreaseLimitController?.populateExpenseField(
                    etMaintainanceExpenses,
                    mHashExpenseDetail["MAINTENANCE_EXPENSES"],
                    tvMaintainanceExpenses
                )
                mIncreaseLimitController?.populateExpenseField(
                    etMonthlyCreditPayments,
                    mHashExpenseDetail["MONTHLY_CREDIT_EXPENSES"],
                    tvMonthlyCreditPayments
                )
                mIncreaseLimitController?.populateExpenseField(
                    etOtherExpenses,
                    mHashExpenseDetail["OTHER_EXPENSES"],
                    tvOtherExpenses
                )
            }
            mIncreaseLimitController?.dynamicLayoutPadding(llSupplyExpenseContainer)
            val activity: Activity? = activity
            (activity as? CLIPhase2Activity)?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            llMortgagePayment?.requestFocus()
        }
    }

    private fun SupplyExpenseDetailFragmentBinding.init() {
        imInfo.setOnClickListener(this@SupplyExpensesDetailFragment)
        val increaseLimitController = IncreaseLimitController(activity)
        increaseLimitController.setQuarterHeight(llEmptyLayout)
        llMortgagePayment?.setOnClickListener(this@SupplyExpensesDetailFragment)
        llRentalPayment?.setOnClickListener(this@SupplyExpensesDetailFragment)
        llMaintainanceExpenses?.setOnClickListener(this@SupplyExpensesDetailFragment)
        llMonthlyCreditPayments?.setOnClickListener(this@SupplyExpensesDetailFragment)
        includeCliNextButton.llNextButtonLayout?.setOnClickListener(this@SupplyExpensesDetailFragment)
        llOtherExpensesContainer?.setOnClickListener(this@SupplyExpensesDetailFragment)
        currencyEditTextParams(etMortgagePayments)
        currencyEditTextParams(etRentalPayments)
        currencyEditTextParams(etMaintainanceExpenses)
        currencyEditTextParams(etMonthlyCreditPayments)
        currencyEditTextParams(etOtherExpenses)
        etMortgagePayments?.addTextChangedListener(GenericTextWatcher(etMortgagePayments))
        etRentalPayments?.addTextChangedListener(GenericTextWatcher(etRentalPayments))
        etMaintainanceExpenses?.addTextChangedListener(GenericTextWatcher(etMaintainanceExpenses))
        etMonthlyCreditPayments?.addTextChangedListener(GenericTextWatcher(etMonthlyCreditPayments))
        etOtherExpenses?.addTextChangedListener(GenericTextWatcher(etOtherExpenses))
        etOtherExpenses?.onFocusChangeListener = this@SupplyExpensesDetailFragment
        includeCliNextButton.llNextButtonLayout?.setOnClickListener(this@SupplyExpensesDetailFragment)
        includeCliNextButton.btnContinue.setOnClickListener(this@SupplyExpensesDetailFragment)
        includeCliNextButton.btnContinue.text = requireContext().resources.getString(R.string.next)
        includeCliNextButton.btnContinue.contentDescription = getString(R.string.expensesNextButton)
    }

    override fun onClick(v: View) {
        binding.apply {
            MultiClickPreventer.preventMultiClick(v)
            when (v.id) {
                R.id.llMortgagePayment -> mIncreaseLimitController?.populateExpenseField(
                    etMortgagePayments,
                    tvMortgagePayments,
                    activity
                )
                R.id.llRentalPayment -> mIncreaseLimitController?.populateExpenseField(
                    etRentalPayments,
                    tvRentalPayments,
                    activity
                )
                R.id.llMaintainanceExpenses -> mIncreaseLimitController?.populateExpenseField(
                    etMaintainanceExpenses,
                    tvMaintainanceExpenses,
                    activity
                )
                R.id.llMonthlyCreditPayments -> mIncreaseLimitController?.populateExpenseField(
                    etMonthlyCreditPayments,
                    tvMonthlyCreditPayments,
                    activity
                )
                R.id.llOtherExpensesContainer -> mIncreaseLimitController?.populateExpenseField(
                    etOtherExpenses,
                    tvOtherExpenses,
                    activity
                )
                R.id.imInfo -> {
                    val act = activity ?: return
                    val cliPhase2Activity = act as CLIPhase2Activity
                    val supplyIncomeInfoFragment =
                        newInstance(SupplyInfoDetailFragment.SupplyDetailViewType.EXPENSE)
                    supplyIncomeInfoFragment.show(
                        cliPhase2Activity.supportFragmentManager,
                        "SupplyInfoDetailFragment"
                    )
                }
                R.id.llNextButtonLayout, R.id.btnContinue -> {
                    val activity: Activity? = activity
                    if (activity != null) {
                        val firebaseEvent = (activity as CLIPhase2Activity).getFirebaseEvent()
                        firebaseEvent?.forIncomeExpense()
                    }
                    val increaseLimitController = IncreaseLimitController(getActivity())
                    val hmExpenseMap = increaseLimitController.expenseHashMap(
                        etMortgagePayments,
                        etRentalPayments,
                        etMaintainanceExpenses,
                        etMonthlyCreditPayments,
                        etOtherExpenses
                    )
                    val bundle = Bundle()
                    bundle.putSerializable(
                        IncreaseLimitController.INCOME_DETAILS,
                        mHashIncomeDetail
                    )
                    bundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, hmExpenseMap)
                    bundle.putBoolean(IncreaseLimitController.FROM_EXPENSE_SCREEN, true)
                    val ocFragment = OfferCalculationFragment()
                    ocFragment.setStepIndicatorListener(mCliStepIndicatorListener)
                    ocFragment.arguments = bundle
                    val fragmentUtils = FragmentUtils()
                    fragmentUtils.nextFragment(
                        activity as AppCompatActivity?,
                        fragmentManager?.beginTransaction(),
                        ocFragment,
                        R.id.cli_steps_container
                    )
                }
            }
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        binding.apply {
            if (hasFocus) {
                if (etOtherExpenses?.hasFocus() == true && etOtherExpensesWasTouched) {
                    nsSupplyExpense?.post {
                        ObjectAnimator.ofInt(
                            nsSupplyExpense,
                            "scrollY",
                            nsSupplyExpense.bottom
                        ).setDuration(300).start()
                    }
                }
            }
        }
    }

    private inner class GenericTextWatcher(private val view: View) :
        TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(
            charSequence: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

        override fun afterTextChanged(editable: Editable) {
            val currentAmount = editable.toString()
            when (view.id) {
                R.id.etMortgagePayments -> {
                    etMortgagePaymentsWasEdited =
                        IncreaseLimitController.validateExpenseAmount(currentAmount)
                    enableNextButton()
                }
                R.id.etRentalPayments -> {
                    etRentalPaymentsWasEdited =
                        IncreaseLimitController.validateExpenseAmount(currentAmount)
                    enableNextButton()
                }
                R.id.etMaintainanceExpenses -> {
                    etMaintainanceExpensesWasEdited =
                        IncreaseLimitController.validateExpenseAmount(currentAmount)
                    enableNextButton()
                }
                R.id.etMonthlyCreditPayments -> {
                    etMonthlyCreditPaymentsWasEdited =
                        IncreaseLimitController.validateExpenseAmount(currentAmount)
                    enableNextButton()
                }
                R.id.etOtherExpenses -> {
                    etOtherExpensesWasEdited =
                        IncreaseLimitController.validateExpenseAmount(currentAmount)
                    enableNextButton()
                }
                else -> {}
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun nextFocusEditText() {
        binding.apply {
            etMortgagePayments?.setOnEditorActionListener { exampleView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    llRentalPayment?.performClick()
                }
                false
            }
            etRentalPayments?.setOnEditorActionListener { exampleView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    llMaintainanceExpenses?.performClick()
                }
                false
            }
            etMaintainanceExpenses?.setOnEditorActionListener { exampleView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    llMonthlyCreditPayments?.performClick()
                }
                false
            }
            etMonthlyCreditPayments?.setOnEditorActionListener { exampleView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    llOtherExpensesContainer?.performClick()
                }
                false
            }
            etOtherExpenses?.setOnTouchListener { v: View?, event: MotionEvent? ->
                etOtherExpensesWasTouched = true
                false
            }
            etOtherExpenses?.setOnEditorActionListener { exampleView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    llOtherExpensesContainer?.performClick()
                }
                false
            }
        }
    }

    private fun enableNextButton() {
        if (etMortgagePaymentsWasEdited
            && etRentalPaymentsWasEdited
            && etMaintainanceExpensesWasEdited
            && etMonthlyCreditPaymentsWasEdited
            && etOtherExpensesWasEdited
        ) {
            binding.includeCliNextButton.llNextButtonLayout?.visibility = View.VISIBLE
        } else {
            binding.includeCliNextButton.llNextButtonLayout?.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        binding.includeCliNextButton.llNextButtonLayout.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_EXPENSES)
        binding.includeCliNextButton.llNextButtonLayout.requestFocus()
        val cliPhase2Activity = activity as CLIPhase2Activity?
        cliPhase2Activity?.actionBarBackIcon()
    }
}
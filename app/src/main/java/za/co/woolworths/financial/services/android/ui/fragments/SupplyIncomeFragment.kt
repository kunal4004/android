package za.co.woolworths.financial.services.android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.cli_next_button.*
import kotlinx.android.synthetic.main.supply_income_detail_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.bindStringArray
import za.co.woolworths.financial.services.android.util.CurrencyTextWatcher
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.MultiClickPreventer
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.CLIFragment
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController
import java.util.HashMap
import java.util.LinkedHashMap

class SupplyIncomeFragment : CLIFragment(), View.OnClickListener {

    private var mHashIncomeDetail: HashMap<String, String>? = null
    private var grossMonthlyIncomeWasEdited = false
    private var netMonthlyIncomeWasEdited = false
    private var additionalMonthlyIncomeWasEdited = false
    private var hmExpenseDetail: HashMap<String, String>? = null
    private var mIncreaseLimitController: IncreaseLimitController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mHashIncomeDetail = getSerializable(IncreaseLimitController.INCOME_DETAILS) as? HashMap<String, String>
            hmExpenseDetail = getSerializable(IncreaseLimitController.EXPENSE_DETAILS) as? HashMap<String, String>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.supply_income_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply { mIncreaseLimitController = IncreaseLimitController(this) }
        init()
        mIncreaseLimitController?.populateExpenseField(etGrossMonthlyIncome, mHashIncomeDetail?.get("GROSS_MONTHLY_INCOME"), tvGrossMonthlyIncome)
        mIncreaseLimitController?.populateExpenseField(etNetMonthlyIncome, mHashIncomeDetail?.get("NET_MONTHLY_INCOME"), tvNetMonthlyIncome)
        mIncreaseLimitController?.populateExpenseField(etAdditionalMonthlyIncome, mHashIncomeDetail?.get("ADDITIONAL_MONTHLY_INCOME"), tvAdditionalMonthlyIncome)
        nextFocusEditText()
        mCliStepIndicatorListener.onStepSelected(1)
        mIncreaseLimitController?.dynamicLayoutPadding(llSupplyIncomeContainer)
        llAdditionalMonthlyIncomeLayout?.requestFocus()
        etGrossMonthlyIncome?.isEnabled = false
        etAdditionalMonthlyIncome?.isEnabled = false
        etNetMonthlyIncome?.isEnabled = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }


    private fun init() {
        (activity as? CLIPhase2Activity)?.actionBarBackIcon()

        llGrossMonthlyIncomeLayout?.setOnClickListener(this)
        llNetMonthlyIncomeLayout?.setOnClickListener(this)
        llAdditionalMonthlyIncomeLayout?.setOnClickListener(this)

        imInfo?.setOnClickListener(this)

        etGrossMonthlyIncome?.addTextChangedListener(CurrencyTextWatcher(etGrossMonthlyIncome))
        etNetMonthlyIncome?.addTextChangedListener(CurrencyTextWatcher(etNetMonthlyIncome))
        etAdditionalMonthlyIncome?.addTextChangedListener(CurrencyTextWatcher(etAdditionalMonthlyIncome))
        etGrossMonthlyIncome?.addTextChangedListener(GenericTextWatcher(etGrossMonthlyIncome))
        etNetMonthlyIncome?.addTextChangedListener(GenericTextWatcher(etNetMonthlyIncome))
        etAdditionalMonthlyIncome?.addTextChangedListener(GenericTextWatcher(etAdditionalMonthlyIncome))


        llNextButtonLayout?.setOnClickListener(this)
        btnContinue?.setOnClickListener(this)
        btnContinue?.text = bindString(R.string.next)
    }

    private inner class GenericTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable) {
            val currentAmount = editable.toString()
            when (view.id) {
                R.id.etGrossMonthlyIncome -> {
                    grossMonthlyIncomeWasEdited =
                            IncreaseLimitController.editTextLength(currentAmount)
                    enableNextButton()
                }
                R.id.etNetMonthlyIncome -> {
                    netMonthlyIncomeWasEdited =
                            IncreaseLimitController.editTextLength(currentAmount)
                    enableNextButton()
                }
                R.id.etAdditionalMonthlyIncome -> {
                    additionalMonthlyIncomeWasEdited =
                            IncreaseLimitController.editTextLength(currentAmount)
                    enableNextButton()
                }
            }
        }
    }

    private fun enableNextButton() {
        llNextButtonLayout?.visibility = if (grossMonthlyIncomeWasEdited
                && netMonthlyIncomeWasEdited
                && additionalMonthlyIncomeWasEdited) View.VISIBLE else View.GONE
    }

    private fun nextFocusEditText() {
        etGrossMonthlyIncome?.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                llNetMonthlyIncomeLayout?.performClick()
                true
            } else {
                false
            }
        }
        etNetMonthlyIncome?.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                llAdditionalMonthlyIncomeLayout?.performClick()
                true
            } else {
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        llNetMonthlyIncomeLayout?.requestFocus()
    }


    override fun onResume() {
        super.onResume()
        activity?.let { activity -> Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_INCOME) }
        etGrossMonthlyIncome?.isEnabled = true
        etAdditionalMonthlyIncome?.isEnabled = true
        etNetMonthlyIncome?.isEnabled = true
        llNetMonthlyIncomeLayout?.requestFocus()
    }

    override fun onClick(v: View) {
        MultiClickPreventer.preventMultiClick(v)
        when (v.id) {
            R.id.imInfo -> {
                val arrTitle = bindStringArray(R.array.supply_info_income_title)
                val arrDescription = resources.getStringArray(R.array.supply_info_income_desc)
                val incomeMap = LinkedHashMap<String, String>()
                var position = 0
                val sizeOfArray =  arrTitle?.size ?: 0
                while (position < sizeOfArray) {
                    val itemPosition = arrTitle?.get(position) ?: ""
                    incomeMap[itemPosition] = arrDescription[position]
                    position++
                }
                mIncreaseLimitController?.hideSoftKeyboard(activity)
                Utils.displayValidationMessage(activity,
                        CustomPopUpWindow.MODAL_LAYOUT.SUPPLY_DETAIL_INFO, Gson().toJson(incomeMap, LinkedHashMap::class.java))
            }
            R.id.llGrossMonthlyIncomeLayout -> mIncreaseLimitController?.populateExpenseField(etGrossMonthlyIncome, tvGrossMonthlyIncome, activity)
            R.id.llNetMonthlyIncomeLayout -> mIncreaseLimitController?.populateExpenseField(etNetMonthlyIncome, tvNetMonthlyIncome, activity)
            R.id.llAdditionalMonthlyIncomeLayout -> mIncreaseLimitController?.populateExpenseField(etAdditionalMonthlyIncome, tvAdditionalMonthlyIncome, activity)
            R.id.btnContinue, R.id.llNextButtonLayout -> {
                val fragmentUtils = FragmentUtils(activity)
                val hmIncomeDetail = mIncreaseLimitController!!.incomeHashMap(etGrossMonthlyIncome, etNetMonthlyIncome, etAdditionalMonthlyIncome)
                val bundle = Bundle()
                bundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, hmIncomeDetail)
                bundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, hmExpenseDetail)
                val supplyExpensesDetailFragment = SupplyExpensesDetailFragment()
                supplyExpensesDetailFragment.arguments = bundle
                supplyExpensesDetailFragment.setStepIndicatorListener(mCliStepIndicatorListener)
                (activity as? AppCompatActivity)?.let { activity -> fragmentUtils.nextFragment(activity, fragmentManager, supplyExpensesDetailFragment, R.id.cli_steps_container) }
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CliEditAmountFragmentBinding
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.service.event.BusStation
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.fragments.cli.EnterAmountToSlideFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView.OnKeyPreImeListener
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.escapeDecimal
import za.co.woolworths.financial.services.android.util.KeyboardUtil
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.CLIFragment

class EditSlideAmountFragment : CLIFragment(R.layout.cli_edit_amount_fragment) {

    private lateinit var binding: CliEditAmountFragmentBinding
    private var currentCredit = 0
    private var creditRequestMax = 0
    private val TAG = EditSlideAmountFragment::class.java.simpleName
    private var title: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CliEditAmountFragmentBinding.bind(view)
        init()
        listener()
        arguments?.let { args ->
            val slideAmount = args.getInt("slideAmount")
            currentCredit = args.getInt("currentCredit")
            creditRequestMax = args.getInt("creditRequestMax")
            binding.etAmount.setText(slideAmount.toString())
            binding.etAmount.setSelection(binding.etAmount.text.toString().length)
            title = getString(R.string.amount_too_low_modal_title)
        }
    }

    private fun listener() {
        binding.etAmount.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val slideAmount = binding.etAmount.text.toString()
                if (!TextUtils.isEmpty(slideAmount)) {
                    hideKeyboard()
                    retrieveNumber(slideAmount)
                }
                return@OnEditorActionListener true
            }
            false
        })
        val onKeyPreImeListener = OnKeyPreImeListener {
            val activity: Activity? = activity
            KeyboardUtil.hideSoftKeyboard(activity)
            (activity as? CLIPhase2Activity)?.onBackPressed()
        }
        binding.etAmount.setOnKeyPreImeListener(onKeyPreImeListener)
    }

    private fun retrieveNumber(slideAmount: String) {
        val newAmount = Utils.numericFieldOnly(slideAmount)
        if (newAmount < currentCredit) {
            minAmountMessage(drawnDownAmount(newAmount), newAmount)
        } else if (newAmount > creditRequestMax) {
            maxAmountMessage(drawnDownAmount(newAmount), newAmount)
        } else {
            setDrawnDownOnSlider(newAmount)
        }
    }

    /**
     * TODO :: Replace event bus by onActivityForResult()
     */
    private fun setDrawnDownOnSlider(newAmount: Int) {
        val progressValue = drawnDownAmount(newAmount)
        val activity: Activity? = activity
        if (activity is CLIPhase2Activity) {
            (activity.getApplication() as WoolworthsApplication)
                .bus()
                .send(BusStation(progressValue, newAmount))
            val fm = activity.supportFragmentManager
            fm.popBackStack(
                EditSlideAmountFragment::class.java.simpleName,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun drawnDownAmount(amount: Int): Int {
        //round down to the nearest hundred
        var amount = amount
        amount -= amount % 100
        return amount - currentCredit
    }

    private fun init() {
        binding.etAmount.requestFocus()
        binding.etAmount.addTextChangedListener(onTextChangedListener())
        forceKeyboard(binding.etAmount)
    }

    private fun forceKeyboard(etAmount: WLoanEditTextView?) {
        val activity: Activity? = activity
        if (activity != null) {
            etAmount!!.requestFocus()
            val imm =
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    private fun hideKeyboard() {
        try {
            val activity: Activity? = activity
            if (activity != null) {
                val view = activity.currentFocus
                if (view != null) {
                    val imm =
                        (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val activity: Activity? = activity
            activity?.runOnUiThread {
                binding.etAmount.requestFocus()
                forceKeyboard(binding.etAmount)
                binding.etAmount.setSelection(binding.etAmount.text.toString().length)
            }
        } catch (ignored: Exception) {
        }
        binding.etAmount?.requestFocus()
    }

    private fun minAmountMessage(progressValue: Int, drawnDownAmount: Int) {
        val minAmountDialog = newInstance(
            progressValue,
            drawnDownAmount,
            title!!,
            getString(R.string.amount_too_low_modal_desc).replace(
                "#R".toRegex(),
                escapeDecimal(currentCredit)
            )
        )
        minAmountDialog.show(fragmentManager!!, EnterAmountToSlideFragment::class.java.simpleName)
    }

    private fun maxAmountMessage(progressValue: Int, drawnDownAmount: Int) {
        val minAmountDialog = newInstance(
            progressValue,
            drawnDownAmount,
            title!!,
            getString(R.string.amount_too_high_modal_desc).replace(
                "#R".toRegex(),
                escapeDecimal(creditRequestMax)
            )
        )
        minAmountDialog.show(fragmentManager!!, EnterAmountToSlideFragment::class.java.simpleName)
    }

    override fun onDestroy() {
        super.onDestroy()
        val activity: Activity? = activity
        hideKeyboard()
        if (activity != null) {
            val cliPhase2Activity = activity as CLIPhase2Activity
            cliPhase2Activity.actionBarCloseIcon()
        }
    }

    private fun onTextChangedListener(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.etAmount.removeTextChangedListener(this)
                val retrieveDrawnDownAmount = s.toString().replace("\\s+".toRegex(), "")
                binding.etAmount.setText(
                    Utils.convertToCurrencyWithoutCent(
                        (if (TextUtils.isEmpty(retrieveDrawnDownAmount)) "0" else retrieveDrawnDownAmount).toLong()
                    )
                )
                binding.etAmount.setTextColor(
                    if (binding.etAmount.text.toString()
                            .equals("0", ignoreCase = true)
                    ) Color.TRANSPARENT else Color.BLACK
                )
                setFocusToEditText(binding.etAmount.text.length)
                binding.etAmount.addTextChangedListener(this)
            }
        }
    }

    private fun setFocusToEditText(atPosition: Int) {
        binding.etAmount.apply {
            setSelection(atPosition)
            requestFocus()
            isFocusable = true
            forceKeyboard(this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}
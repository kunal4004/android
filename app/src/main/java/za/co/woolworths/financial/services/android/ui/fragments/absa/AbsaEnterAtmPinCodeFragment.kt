package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_pin_atm_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaCreateAliasRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateCardAndPinRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateSureCheckRequest
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.contracts.IValidatePinCodeDialogInterface
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity.Companion.ERROR_PAGE_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment
import za.co.woolworths.financial.services.android.util.AsteriskPasswordTransformationMethod
import za.co.woolworths.financial.services.android.util.ErrorHandlerView

class AbsaEnterAtmPinCodeFragment : AbsaFragmentExtension(), OnClickListener, IValidatePinCodeDialogInterface, IDialogListener {

    private var mCreditCardNumber: String? = ""

    companion object {
        const val MAXIMUM_PIN_ALLOWED: Int = 3
        fun newInstance(creditAccountInfo: String?) = AbsaEnterAtmPinCodeFragment().apply {
            arguments = Bundle(1).apply {
                putString("creditCardToken", creditAccountInfo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("creditCardToken")) {
                mCreditCardNumber = arguments?.getString("creditCardToken") ?: ""
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        alwaysShowWindowSoftInputMode()
        return inflater.inflate(R.layout.absa_pin_atm_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewsAndEvents()
        createTextListener(edtEnterATMPin)
    }


    private fun initViewsAndEvents() {
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).clearPageTitle()  }
        tvForgotPin.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvForgotPin.setOnClickListener(this)
        ivNavigateToDigitFragment.setOnClickListener(this)
        edtEnterATMPin.transformationMethod = AsteriskPasswordTransformationMethod()
        edtEnterATMPin.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                navigateToFiveDigitCodeFragment()
            }
            handled
        }

        edtEnterATMPin.setOnKeyListener(OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {

                return@OnKeyListener (pbEnterAtmPin.visibility == VISIBLE)
            }
            false
        })
    }

    private fun navigateToFiveDigitCodeFragment() {
        if ((edtEnterATMPin.length() - 1) >= MAXIMUM_PIN_ALLOWED && pbEnterAtmPin.visibility != VISIBLE) {
            activity?.let {
                val pinCode = edtEnterATMPin.text.toString()
                progressIndicator(VISIBLE)
                mCreditCardNumber?.let { creditCardNumber -> ValidateATMPinCode(creditCardNumber, pinCode, this).make() }
           }
        }
    }

    private fun progressIndicator(state: Int) {
        pbEnterAtmPin?.visibility = state
        activity?.let { pbEnterAtmPin?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(it, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN) }
    }

    private fun createTextListener(edtEnterATMPin: EditText?) {
        var previousLength = 0
        edtEnterATMPin?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (previousLength > edtEnterATMPin.length()) { // detect backspace
                    deletePin((edtEnterATMPin.length()))
                } else {
                    updateEnteredPin((edtEnterATMPin.length() - 1))
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun updateEnteredPin(pinEnteredLength: Int) {
        //Submit button will be enabled when the pin length is 4 & above(at most 5 : As EditText maxLength="5" )
        if (pinEnteredLength > -1) {
            if (pinEnteredLength >= MAXIMUM_PIN_ALLOWED) {
                ivNavigateToDigitFragment.alpha = 1.0f
                ivNavigateToDigitFragment.isEnabled = true
            }
        }
    }

    private fun deletePin(pinEnteredLength: Int) {
        if (pinEnteredLength > -1) {
            if (pinEnteredLength <= MAXIMUM_PIN_ALLOWED) {
                ivNavigateToDigitFragment.alpha = 0.5f
                ivNavigateToDigitFragment.isEnabled = false
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvForgotPin -> {
                activity?.let {
                    if (pbEnterAtmPin.visibility == VISIBLE) return

                    hideKeyboard()
                    val openDialogFragment =
                            GotITDialogFragment.newInstance(getString(R.string.absa_forgot_atm_pin_code_title),
                                    getString(R.string.absa_forgot_atm_pin_code_desc), getString(R.string.cli_got_it),
                                    this)
                    openDialogFragment.show(it.supportFragmentManager, GotITDialogFragment::class.java.simpleName)
                }

            }

            R.id.ivNavigateToDigitFragment -> {
                navigateToFiveDigitCodeFragment()
            }
        }
    }

    override fun onDialogDismissed() {
        alwaysShowWindowSoftInputMode()
    }

    override fun onSuccessHandler(aliasID: String) {
        replaceFragment(
                fragment = AbsaFiveDigitCodeFragment.newInstance(aliasID),
                tag = AbsaFiveDigitCodeFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    override fun onFailureHandler(responseMessage: String, dismissActivity: Boolean) {
        // Navigate back to credit card screen when resultMessage is failed or rejected.
        cancelRequest()
        progressIndicator(INVISIBLE)
        clearPin()
        when {
            responseMessage.trim().contains("card number and pin validation failed!", true) -> {
                ErrorHandlerView(activity).showToast(getString(R.string.incorrect_pin_alert))
            }
            responseMessage.trim().contains("218-invalid card status.", true) -> {
                showErrorScreen(ErrorHandlerActivity.ATM_PIN_LOCKED)
            }
            else -> {
                showErrorScreen(ErrorHandlerActivity.COMMON, responseMessage)
            }
        }

    }

    override fun onFatalError(error: VolleyError?) {
        progressIndicator(GONE)
        clearPin()
        if (error is NoConnectionError) ErrorHandlerView(activity).showToast() else showErrorScreen(ErrorHandlerActivity.COMMON)
    }

    override fun onResume() {
        super.onResume()
        clearPin()
    }

    private fun clearPin() {
        edtEnterATMPin?.apply {
            text.clear()
            showKeyboard(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest()
    }

    private fun cancelRequest() {
        cancelVolleyRequest(AbsaValidateCardAndPinRequest::class.java.simpleName)
        cancelVolleyRequest(AbsaValidateSureCheckRequest::class.java.simpleName)
        cancelVolleyRequest(AbsaCreateAliasRequest::class.java.simpleName)
    }

    private fun showErrorScreen(errorType: Int, errorMessage: String = "") {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage",errorMessage)
            it.startActivityForResult(intent, ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ERROR_PAGE_REQUEST_CODE) {
            when (resultCode) {
                ErrorHandlerActivity.RESULT_RETRY -> {
                    clearPin()
                    alwaysShowWindowSoftInputMode()
                }
            }
        }
    }

    override fun onDialogButtonAction() {
    }

    override fun onSuccessOFOTPSureCheck(userCellNumber: String?) {
        replaceFragment(
                fragment = AbsaOTPConfirmationFragment.newInstance(userCellNumber),
                tag = AbsaOTPConfirmationFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_otp_confirmation_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaCreateAliasRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaValidateSureCheckRequest
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasResponse
import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.Utils
import java.net.HttpCookie

class AbsaOTPConfirmationFragment : AbsaFragmentExtension(), View.OnClickListener, IDialogListener {

    private var userCellNumber: String? = ""

    companion object {
        const val MAXIMUM_PIN_ALLOWED: Int = 7
        fun newInstance(userCellNumber: String?) = AbsaOTPConfirmationFragment().apply {
            arguments = Bundle(1).apply {
                putString("userCellNumber", userCellNumber)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("userCellNumber")) {
                userCellNumber = arguments?.getString("userCellNumber") ?: ""
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        alwaysShowWindowSoftInputMode()
        return inflater!!.inflate(R.layout.absa_otp_confirmation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewsAndEvents()
        createTextListener(enterOTPEditText)
    }


    private fun initViewsAndEvents() {
        otpDescription.text = getString(R.string.absa_otp_screen_description) + userCellNumber
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).setPageTitle(getString(R.string.absa_registration_title_step_1)) }
        ivNavigateToDigitFragment.setOnClickListener(this)
        enterOTPEditText.setOnKeyPreImeListener { activity?.onBackPressed() }
        enterOTPEditText.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                navigateToFiveDigitCodeFragment()
            }
            handled
        }

        enterOTPEditText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {

                return@OnKeyListener (enterOTPProgressBar.visibility == View.VISIBLE)
            }
            false
        })
    }

    private fun navigateToFiveDigitCodeFragment() {
        if ((enterOTPEditText.length() - 1) >= AbsaEnterAtmPinCodeFragment.MAXIMUM_PIN_ALLOWED && enterOTPProgressBar.visibility != View.VISIBLE) {
            activity?.let {
                val otpToBeVerified = enterOTPEditText.text.toString()
                progressIndicator(View.VISIBLE)
                //mCreditCardNumber?.let { creditCardNumber -> ValidateATMPinCode(creditCardNumber, pinCode, this).make() }
                submitOTPToVerify(otpToBeVerified)
            }
        }
    }

    private fun progressIndicator(state: Int) {
        enterOTPProgressBar?.visibility = state
        activity?.let { enterOTPProgressBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(it, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN) }
        if (state == View.VISIBLE)
            wrongOTP.visibility = View.INVISIBLE
    }

    private fun createTextListener(enterOTPEditText: EditText?) {
        var previousLength = 0
        enterOTPEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (previousLength > enterOTPEditText.length()) { // detect backspace
                    deletePin((enterOTPEditText.length()))
                } else {
                    updateEnteredPin((enterOTPEditText.length() - 1))
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

            R.id.ivNavigateToDigitFragment -> {
                navigateToFiveDigitCodeFragment()
            }
        }
    }

    override fun onDialogDismissed() {
        alwaysShowWindowSoftInputMode()
    }

    fun onSuccessHandler(aliasId: String) {
        replaceFragment(
                fragment = AbsaFiveDigitCodeFragment.newInstance(aliasId),
                tag = AbsaFiveDigitCodeFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    fun showCommonError() {
        cancelRequest()
        progressIndicator(View.INVISIBLE)
        clearPin()
        showErrorScreen(ErrorHandlerActivity.WITH_NO_ACTION)
    }

    fun handleFatalError(error: VolleyError?) {
        progressIndicator(View.GONE)
        clearPin()
        if (error is NoConnectionError) ErrorHandlerView(activity).showToast() else showCommonError()
    }

    override fun onResume() {
        super.onResume()
        clearPin()
    }

    private fun clearPin() {
        enterOTPEditText?.apply {
            text.clear()
            showKeyboard(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest()
    }

    private fun cancelRequest() {
        cancelVolleyRequest(AbsaValidateSureCheckRequest::class.java.simpleName)
        cancelVolleyRequest(AbsaCreateAliasRequest::class.java.simpleName)
    }

    private fun showErrorScreen(errorType: Int) {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onDialogButtonAction() {
    }

    private fun submitOTPToVerify(otpToBeVerified: String) {
        AbsaValidateSureCheckRequest(otpToBeVerified).make(SecurityNotificationType.OTP,
                object : AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> {
                    override fun onSuccess(response: ValidateSureCheckResponse?, cookies: MutableList<HttpCookie>?) {
                        with(response?.result)
                        {
                            when {
                                isNullOrEmpty() -> {
                                    showCommonError()
                                }
                                equals("Rejected", true) -> {
                                    response?.let { if (it.otpRetriesLeft > 0) showWrongOTPMessage() else showCommonError() }
                                }
                                equals("Processed", true) -> {
                                    createAlias()
                                }
                                else -> {
                                    showCommonError()
                                }

                            }
                        }
                    }

                    override fun onFailure(errorMessage: String) {
                        showCommonError()

                    }

                    override fun onFatalError(error: VolleyError?) {
                        handleFatalError(error)
                    }
                })
    }

    fun createAlias() {
        var deviceId = Utils.getAbsaUniqueDeviceID()
        AbsaCreateAliasRequest().make(deviceId, object : AbsaBankingOpenApiResponse.ResponseDelegate<CreateAliasResponse> {

            override fun onSuccess(response: CreateAliasResponse?, cookies: MutableList<HttpCookie>?) {
                response?.apply {

                    if (header?.resultMessages?.size == 0 || aliasId != null) {
                        onSuccessHandler(aliasId)
                    } else {
                        showCommonError()
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                showCommonError()
            }

            override fun onFatalError(error: VolleyError?) {
                handleFatalError(error)
            }
        })
    }

    fun showWrongOTPMessage() {
        wrongOTP.visibility = View.VISIBLE
        cancelRequest()
        progressIndicator(View.INVISIBLE)
        clearPin()
    }

}
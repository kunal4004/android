package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaOtpConfirmationFragmentBinding
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toMaskABSAPhoneNumber
import za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel.AbsaIntegrationViewModel
import za.co.woolworths.financial.services.android.util.ErrorHandlerView

class AbsaOTPConfirmationFragment : AbsaFragmentExtension(R.layout.absa_otp_confirmation_fragment), View.OnClickListener, IDialogListener {

    private lateinit var binding: AbsaOtpConfirmationFragmentBinding
    private var mCreditCardToken: String? = null
    private var userCellNumber: String? = ""

    private val mViewModel: AbsaIntegrationViewModel by viewModels()

    companion object {
        const val MAXIMUM_PIN_ALLOWED: Int = 7
        fun newInstance(userCellNumber: String?, creditCardToken: String?) = AbsaOTPConfirmationFragment().apply {
            arguments = Bundle(1).apply {
                putString("userCellNumber", userCellNumber)
                putString("creditCardToken", creditCardToken)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("userCellNumber")) {
                userCellNumber = arguments?.getString("userCellNumber") ?: ""
                if (containsKey("creditCardToken")) {
                    mCreditCardToken = arguments?.getString("creditCardToken") ?: ""
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alwaysShowWindowSoftInputMode()
        binding = AbsaOtpConfirmationFragmentBinding.bind(view)

        with(binding) {
            initViewsAndEvents()
            createTextListener(enterOTPEditText)
            absaApiResultObservers()
        }
    }

    private fun AbsaOtpConfirmationFragmentBinding.absaApiResultObservers() {
        with(mViewModel){
            createAliasId.observe(viewLifecycleOwner) { aliasId ->
                replaceFragment(
                    fragment = AbsaSecurityCheckSuccessfulFragment.newInstance(
                        aliasId,
                        mCreditCardToken
                    ),
                    tag = AbsaSecurityCheckSuccessfulFragment::class.java.simpleName,
                    containerViewId = R.id.flAbsaOnlineBankingToDevice,
                    allowStateLoss = false
                )
            }

            failureHandler.observe(viewLifecycleOwner) { failure ->
                progressIndicator(View.GONE)
                clearPin()
                when (failure) {
                    is AbsaApiFailureHandler.NoInternetApiFailure -> ErrorHandlerView(activity).showToast()
                    is AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidValidateSureCheckContinuePolling -> {
                        showCommonError()
                    }
                    is AbsaApiFailureHandler.HttpException -> handleFatalError(failure)
                    else -> showCommonError()
                }
            }

            validateSureCheckResponseProperty.observe(viewLifecycleOwner) { validateSureCheckResponseProperty ->
                when (validateSureCheckResponseProperty?.result?.lowercase()) {
                    "rejected" -> {
                        validateSureCheckResponseProperty.let { if (it.otpRetriesLeft > 0) showWrongOTPMessage() else showMaxOTPError() }
                    }
                    "processed" -> {
                        fetchCreateAlias()
                    }
                    else -> showCommonError()
                }
            }
        }
    }

    private fun AbsaOtpConfirmationFragmentBinding.initViewsAndEvents() {
        val description = "${bindString(R.string.absa_otp_screen_description)} ${userCellNumber?.toMaskABSAPhoneNumber()}"
        otpDescription.text = description
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).setPageTitle(getString(R.string.absa_registration_title_step_1)) }
        ivNavigateToDigitFragment.setOnClickListener(this@AbsaOTPConfirmationFragment)
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

    private fun AbsaOtpConfirmationFragmentBinding.navigateToFiveDigitCodeFragment() {
        if ((enterOTPEditText.length() - 1) >= AbsaEnterAtmPinCodeFragment.MAXIMUM_PIN_ALLOWED && enterOTPProgressBar.visibility != View.VISIBLE) {
            activity?.let {
                val otpToBeVerified = enterOTPEditText.text.toString()
                progressIndicator(View.VISIBLE)
                submitOTPToVerify(otpToBeVerified)
            }
        }
    }

    private fun AbsaOtpConfirmationFragmentBinding.progressIndicator(state: Int) {
        enterOTPProgressBar?.visibility = state
        activity?.let { enterOTPProgressBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(it, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN) }
        if (state == View.VISIBLE)
            wrongOTP?.visibility = View.INVISIBLE
    }

    private fun AbsaOtpConfirmationFragmentBinding.createTextListener(enterOTPEditText: EditText?) {
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

    private fun AbsaOtpConfirmationFragmentBinding.updateEnteredPin(pinEnteredLength: Int) {
        //Submit button will be enabled when the pin length is 4 & above(at most 5 : As EditText maxLength="5" )
        if (pinEnteredLength > -1) {
            if (pinEnteredLength >= MAXIMUM_PIN_ALLOWED) {
                ivNavigateToDigitFragment.alpha = 1.0f
                ivNavigateToDigitFragment.isEnabled = true
            }
        }
    }

    private fun AbsaOtpConfirmationFragmentBinding.deletePin(pinEnteredLength: Int) {
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
                binding.navigateToFiveDigitCodeFragment()
            }
        }
    }

    override fun onDialogDismissed() {
        alwaysShowWindowSoftInputMode()
    }

    private fun AbsaOtpConfirmationFragmentBinding.showCommonError() {
        progressIndicator(View.INVISIBLE)
        clearPin()
        showErrorScreen(ErrorHandlerActivity.COMMON)
    }

    private fun AbsaOtpConfirmationFragmentBinding.showMaxOTPError() {
        progressIndicator(View.INVISIBLE)
        clearPin()
        showErrorScreen(ErrorHandlerActivity.WITH_NO_ACTION)
    }

    private fun AbsaOtpConfirmationFragmentBinding.handleFatalError(failure: AbsaApiFailureHandler) {
        progressIndicator(View.GONE)
        clearPin()
        when(failure){
            is AbsaApiFailureHandler.NoInternetApiFailure  -> { ErrorHandlerView(activity).showToast() }
            else -> showCommonError()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.enterOTPEditText?.apply {
            showKeyboard(this)
        }
    }

    private fun clearPin() {
        binding.enterOTPEditText?.apply {
            text.clear()
            showKeyboard(this)
        }
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
        mViewModel.fetchValidateSureCheckForOTP(otpToBeVerified)
    }

    private fun AbsaOtpConfirmationFragmentBinding.showWrongOTPMessage() {
        wrongOTP?.visibility = View.VISIBLE
        progressIndicator(View.INVISIBLE)
        clearPin()
    }

}
package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaPinAtmFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.contracts.IValidatePinCodeDialogInterface
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity.Companion.ERROR_PAGE_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.account.GeneralErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler
import za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel.AbsaIntegrationViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment
import za.co.woolworths.financial.services.android.util.AsteriskPasswordTransformationMethod
import za.co.woolworths.financial.services.android.util.ErrorHandlerView

class AbsaEnterAtmPinCodeFragment : AbsaFragmentExtension(R.layout.absa_pin_atm_fragment), OnClickListener, IValidatePinCodeDialogInterface, IDialogListener {

    private lateinit var binding: AbsaPinAtmFragmentBinding
    private lateinit var mActivityResultLaunch: ActivityResultLauncher<Intent>
    private var mCreditCardToken: String? = ""
    private val mViewModel: AbsaIntegrationViewModel by viewModels()

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
                mCreditCardToken = arguments?.getString("creditCardToken") ?: ""
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AbsaPinAtmFragmentBinding.bind(view)

        with(binding) {
            initViewsAndEvents()
            createTextListener(binding.edtEnterATMPin)

            mActivityResultLaunch = registerForActivityResult(
                StartActivityForResult()
            ) { result ->
                if (result.resultCode == -1) { // finish activity when we  navigate back from blocked pin dialog
                    activity?.apply {
                        finish()
                        overridePendingTransition(0, 0)
                    }
                }
            }

            absaApiResultObservers()
        }
    }

    private fun AbsaPinAtmFragmentBinding.absaApiResultObservers() {
        with(mViewModel) {
            failureHandler.observe(viewLifecycleOwner) { failure ->
                progressIndicator(GONE)
                clearPin()
                when (failure) {
                    is AbsaApiFailureHandler.FeatureValidateCardAndPin.ValidateCardAndPinStatusCodeInvalid -> {
                        onFailureHandler(failure.message ?: "", failure.isActivityRunning)
                    }

                    is AbsaApiFailureHandler.FeatureValidateCardAndPin.ValidateSureCheckStatusCodeInvalid -> {
                        onFailureHandler(failure.message ?: "", false)
                    }

                    is AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidValidateSureCheckContinuePolling -> {
                        onFailureHandler(failure.message ?: "", failure.isActivityRunning)
                    }

                    is AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidValidateSureCheckFailedMessage -> {
                        onFailureHandler(failure.message ?: "", failure.isActivityRunning)
                    }
                    is AbsaApiFailureHandler.HttpException -> {
                        showErrorScreen(ErrorHandlerActivity.COMMON)
                    }
                    is AbsaApiFailureHandler.NoInternetApiFailure -> activity?.let {
                        ErrorHandlerView(it).showToast()
                    }

                    is AbsaApiFailureHandler.AppServerFailure.GeneralFailure -> {
                        showErrorScreen(ErrorHandlerActivity.COMMON)
                    }

                    else -> return@observe
                }
            }

            cellNumber.observe(viewLifecycleOwner
            ) { cellNumber ->
                cellNumber?.apply {
                    replaceFragment(
                        fragment = AbsaOTPConfirmationFragment.newInstance(this, mCreditCardToken),
                        tag = AbsaOTPConfirmationFragment::class.java.simpleName,
                        containerViewId = R.id.flAbsaOnlineBankingToDevice,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right
                    )
                    clearAliasIdAndCellphoneNumber()
                    inProgress(false)
                }
            }

            isLoading.observe(viewLifecycleOwner) { isInProgress ->
                pbEnterAtmPin?.visibility = when (isInProgress) {
                    true -> VISIBLE
                    else -> GONE
                }
            }

            createAliasId.observe(viewLifecycleOwner) { aliasId ->
                aliasId?.let { aliasID ->
                    replaceFragment(
                        fragment = AbsaSecurityCheckSuccessfulFragment.newInstance(
                            aliasID,
                            mCreditCardToken
                        ),
                        tag = AbsaSecurityCheckSuccessfulFragment::class.java.simpleName,
                        containerViewId = R.id.flAbsaOnlineBankingToDevice,
                        allowStateLoss = false
                    )
                    clearAliasIdAndCellphoneNumber()
                    inProgress(false)
                }
            }
        }
    }

    private fun AbsaPinAtmFragmentBinding.initViewsAndEvents() {
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).clearPageTitle()  }
        tvForgotPin.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvForgotPin.setOnClickListener(this@AbsaEnterAtmPinCodeFragment)
        ivNavigateToDigitFragment.setOnClickListener(this@AbsaEnterAtmPinCodeFragment)
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

    private fun AbsaPinAtmFragmentBinding.navigateToFiveDigitCodeFragment() {
        if ((edtEnterATMPin.length() - 1) >= MAXIMUM_PIN_ALLOWED && pbEnterAtmPin.visibility != VISIBLE) {
            activity?.let {
                val pinCode = edtEnterATMPin.text.toString()
                mViewModel.fetchAbsaContentEncryptionKeyId(pinCode,mCreditCardToken)
           }
        }
    }

    private fun AbsaPinAtmFragmentBinding.progressIndicator(state: Int) {
        pbEnterAtmPin?.visibility = state
        mViewModel.inProgress(false)
        activity?.let { pbEnterAtmPin?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(it, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN) }
    }

    private fun AbsaPinAtmFragmentBinding.createTextListener(edtEnterATMPin: EditText?) {
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

    private fun AbsaPinAtmFragmentBinding.updateEnteredPin(pinEnteredLength: Int) {
        //Submit button will be enabled when the pin length is 4 & above(at most 5 : As EditText maxLength="5" )
        if (pinEnteredLength > -1) {
            if (pinEnteredLength >= MAXIMUM_PIN_ALLOWED) {
                ivNavigateToDigitFragment.alpha = 1.0f
                ivNavigateToDigitFragment.isEnabled = true
            }
        }
    }

    private fun AbsaPinAtmFragmentBinding.deletePin(pinEnteredLength: Int) {
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
                    if (binding.pbEnterAtmPin.visibility == VISIBLE) return

                    hideKeyboard()
                    val openDialogFragment =
                            GotITDialogFragment.newInstance(getString(R.string.absa_forgot_atm_pin_code_title),
                                    getString(R.string.absa_forgot_atm_pin_code_desc), getString(R.string.got_it),
                                    this)
                    openDialogFragment.show(it.supportFragmentManager, GotITDialogFragment::class.java.simpleName)
                }

            }

            R.id.ivNavigateToDigitFragment -> {
                binding.navigateToFiveDigitCodeFragment()
            }
        }
    }

    override fun onDialogDismissed() {
        alwaysShowWindowSoftInputMode()
    }

    override fun onSuccessHandler(aliasID: String) {

    }

    override fun onFailureHandler(responseMessage: String, dismissActivity: Boolean) {
        // Navigate back to credit card screen when resultMessage is failed or rejected.
        clearPin()

        activity?.apply {
            when {
                responseMessage.trim().contains("card number and pin validation failed!", true) -> {
                    FirebaseEventDetailManager.pin(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
                    ErrorHandlerView(activity).showToast(getString(R.string.incorrect_pin_alert))
                }
                responseMessage.trim().contains("218-invalid card status.", true) -> {
                    FirebaseEventDetailManager.pin(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
                    activity?.apply {
                        val intent = Intent(this, GeneralErrorHandlerActivity::class.java)
                        mActivityResultLaunch.launch(intent)
                        overridePendingTransition(0,0)
                    }
                }
                else -> {
                    FirebaseEventDetailManager.undefined(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
                    showErrorScreen(ErrorHandlerActivity.COMMON, responseMessage)
                }
            }
        }

    }

    override fun onFatalError() {
        binding.progressIndicator(GONE)
        clearPin()
        showErrorScreen(ErrorHandlerActivity.COMMON)
    }

    override fun onResume() {
        super.onResume()
        clearPin()
    }

    private fun clearPin() {
        binding.edtEnterATMPin?.apply {
            text.clear()
            showKeyboard(this)
        }
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
}
package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_pin_atm_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.contracts.IValidatePinCodeDialogInterface
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment

class AbsaEnterAtmPinCodeFragment : AbsaFragmentExtension(), View.OnClickListener, IDialogListener, IValidatePinCodeDialogInterface {

    var mPinImageViewList: MutableList<ImageView>? = null
    private var mCreditAccountInfo: String? = ""

    companion object {
        fun newInstance(creditAccountInfo: String?) = AbsaEnterAtmPinCodeFragment().apply {
            arguments = Bundle(1).apply {
                putString("accountNumber", creditAccountInfo)
            }
        }

        const val MAXIMUM_PIN_ALLOWED: Int = 3
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_pin_atm_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBundleArguments()
        initViewsAndEvents()
        maskPinNumber()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun getBundleArguments() {
        val bundle: Bundle? = arguments

        mCreditAccountInfo = arguments?.getString("accountNumber") ?: ""
    }

    private fun maskPinNumber() {
        tvABSACardNumber?.setText(getString(R.string.absa_biometric_please_card_number, maskedCardNumberWithSpaces(mCreditAccountInfo)))
    }

    private fun initViewsAndEvents() {
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4)
        ivPin5.visibility = View.GONE
        tvForgotPin.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvForgotPin.setOnClickListener(this)
        ivNavigateToDigitFragment.setOnClickListener(this)
        edtEnterATMPin.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                navigateToFiveDigitCodeFragment()
            }
            handled
        }
    }

    private fun navigateToFiveDigitCodeFragment() {
        if ((edtEnterATMPin.length() - 1) == AbsaEnterAtmPinCodeFragment.MAXIMUM_PIN_ALLOWED) {
            activity?.let {
                val pinCode = edtEnterATMPin.text.toString()
                val fm = (it as? AppCompatActivity)?.supportFragmentManager
                val validateCardAndPinDialogFragment = AbsaValidateCardAndPinDialogFragment.newInstance("4103744472666291", "8667")
                // Set the calling fragment for this dialog.
                validateCardAndPinDialogFragment.setTargetFragment(this, 0)
                validateCardAndPinDialogFragment.show(fm, AbsaValidateCardAndPinDialogFragment::class.java.simpleName)
            }
        }
    }

    private fun createTextListener(edtEnterATMPin: EditText?) {
        var previousLength = 0
        edtEnterATMPin?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (previousLength > edtEnterATMPin.length()) { // detect backspace
                    deletePin((edtEnterATMPin.length()), mPinImageViewList!!)
                } else {
                    updateEnteredPin((edtEnterATMPin.length() - 1), mPinImageViewList!!)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun updateEnteredPin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        if (pinEnteredLength > -1) {//Check to prevent mutableList[-1] when navigates back
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_fill)
            if (pinEnteredLength == MAXIMUM_PIN_ALLOWED) {
                ivNavigateToDigitFragment.alpha = 1.0f
                ivNavigateToDigitFragment.isEnabled = true
            }
        }
    }

    private fun deletePin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        if (pinEnteredLength > -1) {
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_empty)
            if (pinEnteredLength <= MAXIMUM_PIN_ALLOWED) {
                ivNavigateToDigitFragment.alpha = 0.5f
                ivNavigateToDigitFragment.isEnabled = false
            }
        }
    }

    private fun clearPinImage(listOfPin: MutableList<ImageView>) {
        listOfPin.forEach {
            it.setImageResource(R.drawable.pin_empty)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvForgotPin -> {
                activity?.let {
                    hideKeyboard()
                    val openDialogFragment =
                            GotITDialogFragment.newInstance(getString(R.string.absa_forgot_atm_pin_code_title),
                                    getString(R.string.absa_forgot_atm_pin_code_desc),
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
        showKeyboard(edtEnterATMPin)
    }

    override fun onSuccessHandler(jSession: JSession) {
        replaceFragment(
                fragment = AbsaFiveDigitCodeFragment.newInstance(jSession),
                tag = AbsaFiveDigitCodeFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    override fun onFailureHandler(responseMessage: String) {
        clearPin()
    }

    override fun onResume() {
        super.onResume()
        clearPin()
    }

    private fun clearPin() {
        edtEnterATMPin?.apply {
            clearPinImage(mPinImageViewList!!)
            text.clear()
            showKeyboard(this)
        }
    }

}
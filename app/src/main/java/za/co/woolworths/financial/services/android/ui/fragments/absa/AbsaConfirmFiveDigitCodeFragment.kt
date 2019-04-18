package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_five_digit_code_fragment.*
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import za.co.absa.openbankingapi.woolworths.integration.AbsaRegisterCredentialRequest
import za.co.woolworths.financial.services.android.contracts.IVibrateComplete
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView

class AbsaConfirmFiveDigitCodeFragment : AbsaFragmentExtension(), View.OnClickListener, IVibrateComplete {

    private var mPinImageViewList: MutableList<ImageView>? = null
    private var mBundleFiveDigitCodePinCode: Int? = null
    private var mShakeAnimation: Animation? = null
    private var mVibrateComplete: IVibrateComplete? = null
    private var mJSession: String? = null
    private var mAliasId: String? = null
    private var mDeviceId: String? = null

    companion object {
        private const val MAXIMUM_PIN_ALLOWED: Int = 4
        private const val VIBRATE_DURATION: Long = 300
        private const val FIVE_DIGIT_PIN_CODE = "FIVE_DIGIT_PIN_CODE"
        private const val JSESSION = "JSESSION"
        private const val ALIAS_ID = "ALIAS_ID"
        private const val DEVICE_ID = "DEVICE_ID"

        fun newInstance(fiveDigitCodePinCode: Int, jSession: String?, aliasId: String?, deviceId: String?) = AbsaConfirmFiveDigitCodeFragment().apply {
            arguments = Bundle(4).apply {
                putInt(FIVE_DIGIT_PIN_CODE, fiveDigitCodePinCode)
                putString(JSESSION, jSession)
                putString(ALIAS_ID, aliasId)
                putString(DEVICE_ID, deviceId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        arguments?.apply {
            mBundleFiveDigitCodePinCode = getInt(FIVE_DIGIT_PIN_CODE, 0)
            getString(JSESSION)?.apply { mJSession = this }
            getString(ALIAS_ID)?.apply { mAliasId = this }
            getString(DEVICE_ID)?.apply { mDeviceId = this }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_five_digit_code_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewsAndEvents()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun initViewsAndEvents() {
        tvEnterYourPin.setText(getString(R.string.absa_confirm_five_digit_code_title))
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
        ivEnterFiveDigitCode.setOnClickListener(this)
        edtEnterATMPin.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                navigateToAbsaBiometricScreen()
            }
            handled
        }
    }

    private fun navigateToAbsaBiometricScreen() {
        if ((edtEnterATMPin.length() - 1) == MAXIMUM_PIN_ALLOWED) {
            val fiveDigitPin = edtEnterATMPin.text.toString()
            if (fiveDigitPin.toInt() == mBundleFiveDigitCodePinCode) {
                navigateToAbsaPinCodeSuccessFragment(mAliasId, mDeviceId, fiveDigitPin, mJSession)
            } else {
                ErrorHandlerView(activity).showToast(getString(R.string.passcode_not_match_alert))
            }
        }
    }

    private fun navigateToAbsaPinCodeSuccessFragment(aliasId: String?, deviceId: String?, fiveDigitPin: String, jSession: String?) {
        hideKeyboard()
        replaceFragment(
                fragment = AbsaPinCodeSuccessFragment.newInstance(aliasId, deviceId, fiveDigitPin,jSession),
                tag = AbsaPinCodeSuccessFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
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
        if (pinEnteredLength > -1) {
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_fill)
            if (pinEnteredLength == MAXIMUM_PIN_ALLOWED) {
                ivEnterFiveDigitCode.alpha = 1.0f
                ivEnterFiveDigitCode.isEnabled = true
            }
        }
    }

    private fun deletePin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        if (pinEnteredLength > -1) {
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_empty)
            if (pinEnteredLength <= MAXIMUM_PIN_ALLOWED) {
                ivEnterFiveDigitCode.alpha = 0.5f
                ivEnterFiveDigitCode.isEnabled = false
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
            R.id.ivEnterFiveDigitCode -> {
                navigateToAbsaBiometricScreen()
            }
        }
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


    override fun onAnimationComplete() {
        clearPin()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest()
    }

    private fun cancelRequest() {
        cancelVolleyRequest(AbsaRegisterCredentialRequest::class.java.simpleName)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.getItem(0)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }
}
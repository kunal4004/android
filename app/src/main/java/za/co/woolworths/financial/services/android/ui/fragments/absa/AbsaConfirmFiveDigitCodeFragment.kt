package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_five_digit_code_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import android.os.Vibrator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import za.co.woolworths.financial.services.android.contracts.IVibrateComplete

class AbsaConfirmFiveDigitCodeFragment : AbsaFragmentExtension(), View.OnClickListener, IVibrateComplete {

    private var mPinImageViewList: MutableList<ImageView>? = null
    private var mFiveDigitCodePinCode: Int? = null
    private var mShakeAnimation: Animation? = null
    private var mVibrateComplete: IVibrateComplete? = null

    companion object {
        fun newInstance(fiveDigitCodePinCode: Int) = AbsaConfirmFiveDigitCodeFragment().apply {
            arguments = Bundle(1).apply {
                putInt("fiveDigitCodePinCode", fiveDigitCodePinCode)
            }
        }

        const val MAXIMUM_PIN_ALLOWED: Int = 4
        const val VIBRATE_DURATION: Long = 300
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
        if (arguments.containsKey("fiveDigitCodePinCode")) {
            mFiveDigitCodePinCode = arguments.getInt("fiveDigitCodePinCode")
        }
        tvEnterYourPin.setText(getString(R.string.absa_confirm_five_digit_code_title))
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
        ivEnterFiveDigitCode.setOnClickListener(this)
        edtEnterATMPin.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                navigateToConfirmFiveDigitCodeFragment()
            }
            handled
        }
    }

    private fun navigateToConfirmFiveDigitCodeFragment() {
        if ((edtEnterATMPin.length() - 1) == MAXIMUM_PIN_ALLOWED) {
            val enteredConfirmPin = edtEnterATMPin.text.toString()
            if (enteredConfirmPin.toInt() == mFiveDigitCodePinCode) {
                replaceFragment(
                        fragment = AbsaBiometricFragment.newInstance(),
                        tag = AbsaBiometricFragment::class.java.simpleName,
                        containerViewId = R.id.flAbsaOnlineBankingToDevice,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right
                )
            } else {
                vibrate(this)
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
                navigateToConfirmFiveDigitCodeFragment()
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

    private fun vibrate(onVibrateComplete: IVibrateComplete) {
        this.mVibrateComplete = onVibrateComplete
        activity?.apply {
            mShakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
            mShakeAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    if (animation === mShakeAnimation) {
                        mVibrateComplete?.onAnimationComplete()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })

            ivEnterFiveDigitCode?.startAnimation(mShakeAnimation)
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(VIBRATE_DURATION)
            }
        }
    }

    override fun onAnimationComplete() {
        clearPin()
    }
}
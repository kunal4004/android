package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import com.awfs.coordination.databinding.AbsaConfirmFiveDigitCodeFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IVibrateComplete
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.Utils

class AbsaConfirmFiveDigitCodeFragment : AbsaFragmentExtension(R.layout.absa_confirm_five_digit_code_fragment), View.OnClickListener, IVibrateComplete {

    private lateinit var binding: AbsaConfirmFiveDigitCodeFragmentBinding
    private var mCreditAccountInfo: String? = null
    private var mPinImageViewList: MutableList<ImageView>? = null
    private var mBundleFiveDigitCodePinCode: Int? = null
    private var mAliasId: String? = null

    companion object {
        private const val MAXIMUM_PIN_ALLOWED: Int = 4
        private const val FIVE_DIGIT_PIN_CODE = "FIVE_DIGIT_PIN_CODE"
        private const val ALIAS_ID = "ALIAS_ID"

        fun newInstance(fiveDigitCodePinCode: Int, aliasId: String?,creditAccountInfo: String?) = AbsaConfirmFiveDigitCodeFragment().apply {
            arguments = Bundle(4).apply {
                putInt(FIVE_DIGIT_PIN_CODE, fiveDigitCodePinCode)
                putString(ALIAS_ID, aliasId)
                putString("creditCardToken", creditAccountInfo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        arguments?.apply {
            mBundleFiveDigitCodePinCode = getInt(FIVE_DIGIT_PIN_CODE, 0)
            getString(ALIAS_ID)?.apply { mAliasId = this }
            mCreditAccountInfo = getString("creditCardToken")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AbsaConfirmFiveDigitCodeFragmentBinding.bind(view)
        binding.initViewsAndEvents()
        binding.createTextListener(binding.edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun AbsaConfirmFiveDigitCodeFragmentBinding.initViewsAndEvents() {
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).setPageTitle(getString(R.string.absa_registration_title_step_3))  }
        tvEnterYourPin?.text = getString(R.string.absa_confirm_five_digit_code_title)
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
        ivEnterFiveDigitCode?.visibility = View.GONE
        completeSetupButton?.visibility = View.VISIBLE
        completeSetupButton?.setOnClickListener(this@AbsaConfirmFiveDigitCodeFragment)
        edtEnterATMPin?.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin?.movementMethod = null
        edtEnterATMPin?.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                navigateToAbsaPinCodeSuccessScreen()
            }
            handled
        }
    }

    private fun AbsaConfirmFiveDigitCodeFragmentBinding.navigateToAbsaPinCodeSuccessScreen() {
        if ((edtEnterATMPin.length() - 1) == MAXIMUM_PIN_ALLOWED) {
            val fiveDigitPin = edtEnterATMPin.text.toString()
            if (completeSetupButton?.isEnabled == true && fiveDigitPin.toInt() == mBundleFiveDigitCodePinCode) {
                navigateToAbsaPinCodeSuccessFragment(mAliasId, fiveDigitPin)
            } else {
                activity?.apply { FirebaseEventDetailManager.passcode(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this) }
                ErrorHandlerView(activity).showToast(getString(R.string.passcode_not_match_alert))
                clearPin()
            }
        }
    }

    private fun navigateToAbsaPinCodeSuccessFragment(aliasId: String?, fiveDigitPin: String) {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ABSA_CC_COMPLETE_SETUP, this) }
        hideKeyboard()
        replaceFragment(
                fragment = AbsaPinCodeSuccessFragment.newInstance(aliasId, fiveDigitPin, mCreditAccountInfo),
                tag = AbsaPinCodeSuccessFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }


    private fun AbsaConfirmFiveDigitCodeFragmentBinding.createTextListener(edtEnterATMPin: EditText?) {
        var previousLength = 0
        edtEnterATMPin?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (previousLength > s.length) { // detect backspace
                    deletePin((s.length), mPinImageViewList!!)
                } else {
                    updateEnteredPin((s.length - 1), mPinImageViewList!!)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun AbsaConfirmFiveDigitCodeFragmentBinding.updateEnteredPin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        if (pinEnteredLength > -1) {
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_fill)
            if (pinEnteredLength == MAXIMUM_PIN_ALLOWED) {
                completeSetupButton.isEnabled = true
            }
        }
    }

    private fun AbsaConfirmFiveDigitCodeFragmentBinding.deletePin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        if (pinEnteredLength > -1) {
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_empty)
            if (pinEnteredLength <= MAXIMUM_PIN_ALLOWED) {
                completeSetupButton.isEnabled = false
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
            R.id.completeSetupButton -> {
                binding.navigateToAbsaPinCodeSuccessScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.clearPin()
    }

    private fun AbsaConfirmFiveDigitCodeFragmentBinding.clearPin() {
        edtEnterATMPin?.apply {
            clearPinImage(mPinImageViewList!!)
            text.clear()
            showKeyboard(this)
        }
    }


    override fun onAnimationComplete() {
        binding.clearPin()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.getItem(0)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }
}
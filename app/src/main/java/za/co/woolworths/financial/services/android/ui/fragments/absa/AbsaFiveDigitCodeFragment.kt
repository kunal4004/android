package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_five_digit_code_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment

class AbsaFiveDigitCodeFragment : AbsaFragmentExtension(), View.OnClickListener {

    private var mPinImageViewList: MutableList<ImageView>? = null
    private var mAliasId: String? = null

    companion object {
        private var mCreditCardToken: String? = null
        private const val MAXIMUM_PIN_ALLOWED: Int = 4
        private const val ALIAS_ID = "ALIAS_ID"

        fun newInstance(aliasId: String?,creditCardToken:String?) = AbsaFiveDigitCodeFragment().apply {
            arguments = Bundle(3).apply {
                putString(ALIAS_ID, aliasId)
                putString("creditCardToken", creditCardToken)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        arguments?.apply {
            getString(ALIAS_ID)?.apply { mAliasId = this }
            mCreditCardToken = getString("creditCardToken") ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.absa_five_digit_code_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewsAndEvents()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun initViewsAndEvents() {
        (activity as? ABSAOnlineBankingRegistrationActivity)?.setPageTitle(getString(R.string.absa_registration_title_step_2))
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
        ivEnterFiveDigitCode?.setOnClickListener(this)
        edtEnterATMPin?.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin?.movementMethod = null
        edtEnterATMPin?.setOnEditorActionListener { _, actionId, _ ->
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
            val enteredPin = edtEnterATMPin.text.toString()
            replaceFragment(
                    fragment = AbsaConfirmFiveDigitCodeFragment.newInstance(enteredPin.toInt(), mAliasId,mCreditCardToken),
                    tag = AbsaConfirmFiveDigitCodeFragment::class.java.simpleName,
                    containerViewId = R.id.flAbsaOnlineBankingToDevice,
                    allowStateLoss = true,
                    enterAnimation = R.anim.slide_in_from_right,
                    exitAnimation = R.anim.slide_to_left,
                    popEnterAnimation = R.anim.slide_from_left,
                    popExitAnimation = R.anim.slide_to_right
            )
        }
    }

    private fun createTextListener(edtEnterATMPin: EditText?) {
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
        edtEnterATMPin?.apply {
            clearPinImage(mPinImageViewList!!)
            text.clear()
            requestFocus()
            showKeyboard(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.getItem(0)?.isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }
}
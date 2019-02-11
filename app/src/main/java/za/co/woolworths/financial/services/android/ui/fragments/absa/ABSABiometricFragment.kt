package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_biometric_fragment.*


class ABSABiometricFragment : Fragment() {

    var mPinImageViewList: MutableList<ImageView>? = null

    companion object {
        fun newInstance() = ABSABiometricFragment()
        const val MAXIMUM_PIN_ALLOWED: Int = 4
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_biometric_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setWindowSoftInputMode()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun initView() {
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4)
        ivPin5.visibility = View.GONE
    }

    private fun setWindowSoftInputMode() {
        activity?.apply {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_fill)
        if (pinEnteredLength == MAXIMUM_PIN_ALLOWED) {

        }
    }

    private fun deletePin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_empty)
    }

    private fun clearPinImage(listOfPin: MutableList<ImageView>) {
        listOfPin.forEach {
            it.setImageResource(R.drawable.pin_empty)
        }
    }

}
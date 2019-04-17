package za.co.woolworths.financial.services.android.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_handler_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity

class ErrorHandlerFragment : Fragment(), View.OnClickListener {

    var errorType: Int = 0

    companion object {
        fun newInstance(errorType: Int) = ErrorHandlerFragment().apply {
            arguments = Bundle(1).apply {
                putInt("errorType", errorType)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.error_handler_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("errorType")) {
                errorType = arguments?.getInt("errorType", 0)!!
            }
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewOnErrorType()
        initListeners()
    }

    private fun initListeners() {

        cancelButton.setOnClickListener(this)
        actionButton.setOnClickListener(this)

    }

    private fun setViewOnErrorType() {
        when (errorType) {
            ErrorHandlerActivity.ATM_PIN_LOCKED -> {
                errorLogo.setImageResource(R.drawable.ic_passcode_locked_icon)
                errorTitle.text = getString(R.string.error_atm_pin_locked_title)
                cancelButton.visibility = View.GONE
                actionButton.text = getString(R.string.absa_biometric_forgot_atm_pin_code)
            }
            ErrorHandlerActivity.PASSCODE_LOCKED -> {
                errorLogo.setImageResource(R.drawable.ic_passcode_locked_icon)
            }
            ErrorHandlerActivity.COMMON -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.unsuccessful_request)
                cancelButton.visibility = View.VISIBLE
                actionButton.text = getString(R.string.retry)
            }
            ErrorHandlerActivity.WITH_NO_ACTION -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.unsuccessful_request)
                actionsLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.cancelButton -> {
                activity?.apply {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            R.id.actionButton -> {
                when (errorType) {
                    ErrorHandlerActivity.ATM_PIN_LOCKED -> {

                    }
                    ErrorHandlerActivity.PASSCODE_LOCKED -> {

                    }
                    ErrorHandlerActivity.COMMON -> {
                        activity?.apply {
                            setResult(ErrorHandlerActivity.RESULT_RETRY)
                            finish()
                        }
                    }
                }
            }
        }
    }

}
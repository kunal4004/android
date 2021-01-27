package za.co.woolworths.financial.services.android.ui.fragments

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_handler_fragment.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment

class ErrorHandlerFragment : Fragment(), View.OnClickListener, IDialogListener {
    override fun onDialogDismissed() {

    }


    companion object {
        var errorType: Int = 0
        var errorMessage: String? = null

        fun newInstance(errorMessage: String, errorType: Int) = ErrorHandlerFragment().withArgs {
                putInt("errorType", errorType)
                putString("errorMessage",errorMessage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.error_handler_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("errorType")) {
                errorType = arguments?.getInt("errorType", 0)!!
            }

            if (containsKey("errorMessage")) {
                errorMessage = getString("errorMessage", "")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewOnErrorType()
        initListeners()
    }

    private fun initListeners() {
        cancelButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        cancelButton.setOnClickListener(this)
        actionButton.setOnClickListener(this)

    }

    private fun setViewOnErrorType() {
        when (errorType) {
            ErrorHandlerActivity.ATM_PIN_LOCKED -> {
                errorLogo.setImageResource(R.drawable.ic_passcode_locked_icon)
                errorTitle.text = context?.getString(R.string.error_atm_pin_locked_title)
                errorDescription.text = context?.getString(R.string.error_atm_pin_locked_desc)
                errorDescription.visibility = View.VISIBLE
                cancelButton.visibility = View.GONE
                actionButton.text = getString(R.string.ok_got_it)
                actionButton.isAllCaps = true
            }
            ErrorHandlerActivity.PASSCODE_LOCKED -> {
                errorLogo.setImageResource(R.drawable.ic_passcode_locked_icon)
                errorTitle.text = getString(R.string.error_passcode_locked_title)
                errorDescription.text = getString(R.string.error_passcode_locked_desc)
                cancelButton.text = getString(R.string.error_action_later)
                actionButton.text = getString(R.string.reset_passcode)
            }
            ErrorHandlerActivity.COMMON -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.absa_general_error_title)
                errorDescription?.text = getString(R.string.absa_common_error_message)
                actionButton.text = getString(R.string.retry)
            }
            ErrorHandlerActivity.WITH_NO_ACTION -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.absa_general_error_title)
                errorDescription.text = getString(R.string.absa_common_error_message)
                actionsLayout.visibility = View.INVISIBLE
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.cancelButton -> {
                setResultBAck(Activity.RESULT_CANCELED)
            }
            R.id.actionButton -> {
                when (errorType) {
                    ErrorHandlerActivity.ATM_PIN_LOCKED -> {
                        setResultBAck(Activity.RESULT_CANCELED)
                    }
                    ErrorHandlerActivity.PASSCODE_LOCKED -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_RESET_PASSCODE)
                    }
                    ErrorHandlerActivity.COMMON -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_RETRY)
                    }

                }
            }
        }
    }

    private fun setResultBAck(resultCode: Int) {
        activity?.apply {
            setResult(resultCode)
            finish()
        }
    }

    override fun onDialogButtonAction() {
    }

}
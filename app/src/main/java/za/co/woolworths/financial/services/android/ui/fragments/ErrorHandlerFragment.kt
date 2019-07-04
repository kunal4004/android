package za.co.woolworths.financial.services.android.ui.fragments

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.app.Fragment
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

    var errorType: Int = 0

    companion object {
        fun newInstance(errorType: Int) = ErrorHandlerFragment().withArgs {
                putInt("errorType", errorType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                errorTitle.text = getString(R.string.error_atm_pin_locked_title)
                errorDescription.visibility = View.INVISIBLE
                cancelButton.visibility = View.GONE
                actionButton.text = getString(R.string.absa_biometric_forgot_atm_pin_code)
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
                errorTitle.text = getString(R.string.unsuccessful_request)
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
                setResultBAck(Activity.RESULT_CANCELED)
            }
            R.id.actionButton -> {
                when (errorType) {
                    ErrorHandlerActivity.ATM_PIN_LOCKED -> {
                        val openDialogFragment =
                                GotITDialogFragment.newInstance(getString(R.string.absa_forgot_atm_pin_code_title),
                                        getString(R.string.absa_forgot_atm_pin_code_desc), getString(R.string.cli_got_it),
                                        this)
                        activity?.let {
                            openDialogFragment.show(it.supportFragmentManager, GotITDialogFragment::class.java.simpleName)
                        }
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
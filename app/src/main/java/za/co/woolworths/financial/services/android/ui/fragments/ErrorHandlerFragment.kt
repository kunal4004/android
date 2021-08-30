package za.co.woolworths.financial.services.android.ui.fragments

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
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
            putString("errorMessage", errorMessage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        setHasOptionsMenu(true)
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
            ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON -> {
                errorLogo?.setImageResource(R.drawable.ic_error_icon)
                errorTitle?.text =
                    getString(R.string.common_error_unfortunately_something_went_wrong)
                errorDescription?.text =
                    if(TextUtils.isEmpty(errorMessage))
                      getString(R.string.common_error_message_without_contact_info)
                    else errorMessage
                actionButton?.text = getString(R.string.retry)
                cancelButton?.visibility = View.GONE
                enableBackButton()
            }
            ErrorHandlerActivity.WITH_NO_ACTION -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.absa_general_error_title)
                errorDescription.text = getString(R.string.absa_common_error_message)
                actionsLayout.visibility = View.INVISIBLE
            }
            ErrorHandlerActivity.LINK_DEVICE_FAILED -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.link_device_error_title)
                errorDescription?.text = getString(R.string.link_device_error_desc)
                actionButton.text = getString(R.string.retry)
                cancelButton.text = getString(R.string.need_help_call_the_center)
            }
            ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION -> {
                errorLogo.setImageResource(R.drawable.ic_error_icon)
                errorTitle.text = getString(R.string.store_email_error_title)
                errorDescription?.text = getString(R.string.store_email_error_desc)
                actionButton.text = getString(R.string.retry)
                cancelButton.text = getString(R.string.need_help_call_the_center)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (errorType) {
            ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON -> {
                menu.clear()
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun enableBackButton() {
        (activity as? ErrorHandlerActivity)?.apply {
            supportActionBar?.let { actionBar ->
                actionBar.setHomeAsUpIndicator(R.drawable.back24)
                actionBar.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.cancelButton -> {
                when (errorType) {
                    ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION, ErrorHandlerActivity.LINK_DEVICE_FAILED -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_CALL_CENTER)
                    }
                    else -> {
                        setResultBAck(Activity.RESULT_CANCELED)
                    }
                }
            }
            R.id.actionButton -> {
                when (errorType) {
                    ErrorHandlerActivity.ATM_PIN_LOCKED -> {
                        setResultBAck(Activity.RESULT_CANCELED)
                    }
                    ErrorHandlerActivity.PASSCODE_LOCKED -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_RESET_PASSCODE)
                    }
                    ErrorHandlerActivity.COMMON, ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_RETRY)
                    }
                    ErrorHandlerActivity.LINK_DEVICE_FAILED -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_RETRY)
                    }
                    ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION -> {
                        setResultBAck(ErrorHandlerActivity.RESULT_RETRY)
                    }
                }
            }
            R.id.home -> {
                activity?.onBackPressed()
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
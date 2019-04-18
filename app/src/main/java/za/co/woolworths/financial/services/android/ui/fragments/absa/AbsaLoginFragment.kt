package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.android.volley.VolleyError
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_login_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaLoginRequest
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.absa.openbankingapi.woolworths.integration.service.VolleyErrorHandler
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.numberkeyboard.NumberKeyboardListener
import java.net.HttpCookie

class AbsaLoginFragment : AbsaFragmentExtension(), NumberKeyboardListener, IDialogListener {

    private var mPinImageViewList: MutableList<ImageView>? = null

    companion object {
        private const val MAXIMUM_PIN_ALLOWED: Int = 4
        private const val technical_error_occurred = "Technical error occurred."
        fun newInstance() = AbsaLoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.absa_login_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alwaysHideWindowSoftInputMode()
        initViewsAndEvents()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun initViewsAndEvents() {
        tvForgotPasscode.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvForgotPasscode.setOnClickListener{
            activity?.let {
                val openDialogFragment =
                        GotITDialogFragment.newInstance(getString(R.string.forgot_passcode),
                               getString(R.string.forgot_passcode_dialog_desc),getString(R.string.cancel),
                                this)
                openDialogFragment.show(it.supportFragmentManager, GotITDialogFragment::class.java.simpleName)
            }
        }
        numberKeyboard.setListener(this)
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
    }

    private fun requestToLogin() {
        if ((edtEnterATMPin.length() - 1) < MAXIMUM_PIN_ALLOWED)
            return
        val userPin = edtEnterATMPin.text.toString()
        val aliasId = SessionDao.getByKey(SessionDao.KEY.ABSA_ALIASID)?.value ?: ""
        val deviceId = SessionDao.getByKey(SessionDao.KEY.ABSA_DEVICEID)?.value ?: ""
        absaLoginRequest(aliasId, deviceId, userPin)

    }

    private fun absaLoginRequest(aliasId: String?, deviceId: String?, userPin: String) {
        activity?.let {
            displayLoginProgress(true)
            AbsaLoginRequest(it).make(userPin, aliasId, deviceId,
                    object : AbsaBankingOpenApiResponse.ResponseDelegate<LoginResponse> {

                        override fun onSuccess(response: LoginResponse?, cookies: MutableList<HttpCookie>?) {
                            response?.apply {
                                if (result?.toLowerCase() == "success") {
                                    successHandler()
                                } else {
                                    failureHandler(resultMessage ?: technical_error_occurred)
                                }
                                /* Work for WOP-3881
                                   Commented because header returning nil
                                    header?.apply {
                                        if (statusCode == "0" || resultMessages.isEmpty()) {
                                            successHandler(response)
                                        } else {
                                            if (statusCode == "1") {
                                                failureHandler(header.resultMessages.first()?.responseMessage
                                                        ?: technical_error_occurred)
                                            }
                                        }
                                    }*/
                            }
                            displayLoginProgress(false)
                        }

                        override fun onFailure(errorMessage: String) {
                            displayLoginProgress(false)
                            failureHandler(errorMessage)
                        }

                        override fun onFatalError(error: VolleyError?) {
                            (activity as? AppCompatActivity)?.apply { error?.let { error -> VolleyErrorHandler(this, error).show() } }
                        }
                    })
        }
    }

    private fun successHandler() {
        tapAndDismissErrorDialog("Login Successful")
    }

    private fun failureHandler(message: String?) {
        cancelRequest()
        // message?.let { tapAndNavigateBackErrorDialog(it) }
        when {
            message?.trim()?.contains("authentication failed", true)!! -> {
                ErrorHandlerView(activity).showToast(getString(R.string.incorrect_passcode_alert))
                clearPin()
            }
            message.trim().contains("credential revoked", true) -> {
                showErrorScreen(ErrorHandlerActivity.PASSCODE_LOCKED)
            }
            else -> {
                showErrorScreen(ErrorHandlerActivity.COMMON)
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
            }
        }
    }

    private fun deletePin(pinEnteredLength: Int, listOfPin: MutableList<ImageView>) {
        if (pinEnteredLength > -1) {
            listOfPin[pinEnteredLength].setImageResource(R.drawable.pin_empty)
            if (pinEnteredLength <= MAXIMUM_PIN_ALLOWED) {

            }
        }
    }

    private fun clearPinImage(listOfPin: MutableList<ImageView>) {
        listOfPin.forEach {
            it.setImageResource(R.drawable.pin_empty)
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

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest()
    }

    private fun cancelRequest() {
        cancelVolleyRequest(AbsaLoginRequest::class.java.simpleName)
    }

    fun displayLoginProgress(state: Boolean) {
        pbLoginProgress.visibility = if (state) VISIBLE else INVISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.getItem(0)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showErrorScreen(errorType: Int) {
        activity.let {
            val intent: Intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE) {
            when (resultCode) {
                ErrorHandlerActivity.RESULT_RETRY -> {
                    clearPin()
                    alwaysShowWindowSoftInputMode()
                }
            }
        }
    }

    override fun onNumberClicked(number: Int) {
        edtEnterATMPin.text = Editable.Factory.getInstance().newEditable(edtEnterATMPin.text.append(number.toString()))
        requestToLogin()
    }

    override fun onLeftAuxButtonClicked() {
    }

    override fun onRightAuxButtonClicked() {
        if (edtEnterATMPin.text.isNotEmpty())
            edtEnterATMPin.text = Editable.Factory.getInstance().newEditable(edtEnterATMPin.text.substring(0, edtEnterATMPin.text.length - 1))
    }

    override fun onDialogDismissed() {

    }

}
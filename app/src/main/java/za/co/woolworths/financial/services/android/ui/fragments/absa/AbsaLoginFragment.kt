package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_login_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaContentEncryptionRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaLoginRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity.Companion.E_SESSION_ID
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity.Companion.NONCE
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment.Companion.CARD
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.numberkeyboard.NumberKeyboardListener
import java.net.HttpCookie

class AbsaLoginFragment : AbsaFragmentExtension(), NumberKeyboardListener, IDialogListener {

    private var mCreditCardNumber: String? = null
    private var mPinImageViewList: MutableList<ImageView>? = null

    companion object {
        private const val MAXIMUM_PIN_ALLOWED: Int = 4
        private const val technical_error_occurred = "Technical error occurred."
        fun newInstance(creditAccountInfo: String?) = AbsaLoginFragment().apply {
            arguments = Bundle(1).apply {
                putString("creditCardToken", creditAccountInfo)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.absa_login_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewsAndEvents()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun initViewsAndEvents() {

        arguments?.apply {
            if (containsKey("creditCardToken")) {
                mCreditCardNumber = arguments?.getString("creditCardToken") ?: ""
            }
        }

        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).clearPageTitle()  }
        tvForgotPasscode.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvForgotPasscode.setOnClickListener {

            if (pbLoginProgress.visibility == VISIBLE) return@setOnClickListener

            activity?.let {

                //Clear content encryption data if any, before making new registration process.
                AbsaContentEncryptionRequest.clearContentEncryptionData()

                val openDialogFragment =
                        GotITDialogFragment.newInstance(getString(R.string.forgot_passcode),
                                getString(R.string.forgot_passcode_dialog_desc), getString(R.string.cancel),
                                this, getString(R.string.reset_passcode))
                openDialogFragment.show(it.supportFragmentManager, GotITDialogFragment::class.java.simpleName)
            }
        }
        numberKeyboard.setListener(this)
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
    }

    private fun requestToLogin() {
        if ((edtEnterATMPin.length() - 1) < MAXIMUM_PIN_ALLOWED)
            return

        val absaSecureCredentials = AbsaSecureCredentials();

        val userPin = edtEnterATMPin.text.toString()
        val aliasId = absaSecureCredentials.aliasId
        val deviceId = Utils.getAbsaUniqueDeviceID()
        absaLoginRequest(aliasId, deviceId, userPin)

    }

    private fun absaLoginRequest(aliasId: String?, deviceId: String?, userPin: String) {

        //Clear content encryption data be fore making login request.
        AbsaContentEncryptionRequest.clearContentEncryptionData()


        activity?.let {
            displayLoginProgress(true)
            AbsaLoginRequest().make(userPin, aliasId, deviceId,
                    object : AbsaBankingOpenApiResponse.ResponseDelegate<LoginResponse> {

                        override fun onSuccess(response: LoginResponse?, cookies: MutableList<HttpCookie>?) {
                            response?.apply {
                                if (result?.toLowerCase() == "success") {
                                    successHandler(this.nonce, this.esessionid)
                                } else {
                                    failureHandler(resultMessage ?: technical_error_occurred)
                                }
                            }
                            displayLoginProgress(false)
                        }

                        override fun onFailure(errorMessage: String) {
                            activity?.apply { FirebaseEventDetailManager.pin(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this) }
                            displayLoginProgress(false)
                            failureHandler(errorMessage)
                        }

                        override fun onFatalError(error: VolleyError?) {
                            displayLoginProgress(false)
                            clearPin()
                            if (error is NoConnectionError){
                                ErrorHandlerView(activity).showToast()
                                activity?.apply { FirebaseEventDetailManager.network(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this) }
                            }else{
                                activity?.apply { FirebaseEventDetailManager.undefined(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this) }
                                showErrorScreen(ErrorHandlerActivity.COMMON)}
                        }
                    })
        }
    }

    private fun successHandler(nonce: String, esessionid: String) {

        activity?.apply {
            Intent(activity, AbsaStatementsActivity::class.java).let {
                it.putExtra(NONCE, nonce)
                it.putExtra(E_SESSION_ID, esessionid)
                it.putExtra(CARD, mCreditCardNumber )
                startActivity(it)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                finish()
            }
        }

        //Clearing up sensitive info.
        mCreditCardNumber = "";
        clearPin()
    }

    private fun failureHandler(message: String?) {
        cancelRequest()
        // message?.let { tapAndNavigateBackErrorDialog(it) }
        activity?.apply {
            when {
                message?.trim()?.contains("authentication failed", true)!! -> {
                    FirebaseEventDetailManager.passcode(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
                    ErrorHandlerView(activity).showToast(getString(R.string.incorrect_passcode_alert))
                }
                message.trim().contains("credential revoked", true) -> {
                    FirebaseEventDetailManager.passcode(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
                    showErrorScreen(ErrorHandlerActivity.PASSCODE_LOCKED)
                }
                else -> {
                    FirebaseEventDetailManager.undefined(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
                    showErrorScreen(ErrorHandlerActivity.COMMON, message)
                }
            }
        }

        //Clearing up sensitive info.
        mCreditCardNumber = "";
        clearPin()
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
        pbLoginProgress?.visibility = if (state) VISIBLE else INVISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.getItem(0)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showErrorScreen(errorType: Int) {
        activity?.let {
            val intent: Intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun showErrorScreen(errorType: Int, errorMessage: String?) {
        activity?.let {
            val intent: Intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage",errorMessage)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE) {
            when (resultCode) {
                ErrorHandlerActivity.RESULT_RETRY -> {
                    clearPin()
                    alwaysHideWindowSoftInputMode()
                }
            }
        }
    }

    override fun onNumberClicked(number: Int) {

        if (pbLoginProgress.visibility == VISIBLE)
            return

        edtEnterATMPin.text = Editable.Factory.getInstance().newEditable(edtEnterATMPin.text.append(number.toString()))
        requestToLogin()
    }

    override fun onLeftAuxButtonClicked() {
    }

    override fun onRightAuxButtonClicked() {
        if (edtEnterATMPin.text.isNotEmpty() && pbLoginProgress.visibility != VISIBLE)
            edtEnterATMPin.text = Editable.Factory.getInstance().newEditable(edtEnterATMPin.text.substring(0, edtEnterATMPin.text.length - 1))
    }

    override fun onDialogButtonAction() {

        activity?.let {
            (it as ABSAOnlineBankingRegistrationActivity).startAbsaRegistration()
        }
    }

}
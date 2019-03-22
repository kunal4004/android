package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_login_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaLoginRequest
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import java.net.HttpCookie

class AbsaLoginFragment : AbsaFragmentExtension() {

    private var mPinImageViewList: MutableList<ImageView>? = null

    companion object {
        private const val MAXIMUM_PIN_ALLOWED: Int = 4
        fun newInstance() = AbsaLoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.absa_login_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewsAndEvents()
        createTextListener(edtEnterATMPin)
        clearPinImage(mPinImageViewList!!)
    }

    private fun initViewsAndEvents() {
        mPinImageViewList = mutableListOf(ivPin1, ivPin2, ivPin3, ivPin4, ivPin5)
        edtEnterATMPin.setOnKeyPreImeListener { activity?.onBackPressed() }
        edtEnterATMPin.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handled = true
                requestToLogin()
            }
            handled
        }
    }

    private fun requestToLogin() {
        if ((edtEnterATMPin.length() - 1) == MAXIMUM_PIN_ALLOWED) {
            val userPin = edtEnterATMPin.text.toString()
            val aliasId = SessionDao.getByKey(SessionDao.KEY.ABSA_ALIASID)?.value ?: ""
            val deviceId = SessionDao.getByKey(SessionDao.KEY.ABSA_DEVICEID)?.value ?: ""
            absaLoginRequest(aliasId, deviceId, userPin)
        } else {
            clearPin()
        }
    }

    private fun absaLoginRequest(aliasId: String?, deviceId: String?, userPin: String) {
        activity?.let {
            displayLoginProgress(true)

            AbsaLoginRequest(it).make(userPin,aliasId, deviceId,
                    object : AbsaBankingOpenApiResponse.ResponseDelegate<LoginResponse> {
                        override fun onSuccess(response: LoginResponse?, cookies: MutableList<HttpCookie>?) {
                            response?.apply {
                                if (!(resultMessage?.length != 0 && aliasId == null)) {
                                    successHandler(this)
                                } else {
                                    failureHandler(resultMessage ?: "")
                                }
                            }

                            displayLoginProgress(false)
                        }

                        override fun onFailure(errorMessage: String) {
                            Log.d("onSuccess", "onFailure")
                            displayLoginProgress(false)
                        }
                    })
        }
    }

    private fun successHandler(response: LoginResponse) {
        //TODO:: handle Success
    }

    private fun failureHandler(message: String?) {
        cancelRequest()
        //TODO: implement failureHandler(response.header!.resultMessages.first?.responseMessage ??
        // "Technical error occured.")
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
        pbLoginProgress.visibility = if (state) View.VISIBLE else View.GONE
    }
}
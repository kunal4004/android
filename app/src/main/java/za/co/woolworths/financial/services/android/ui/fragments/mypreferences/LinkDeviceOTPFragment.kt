package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_enter_otp.*
import kotlinx.android.synthetic.main.fragment_enter_otp.buttonNext
import kotlinx.android.synthetic.main.fragment_enter_otp.didNotReceiveOTPTextView
import kotlinx.android.synthetic.main.fragment_link_device_otp.*
import kotlinx.android.synthetic.main.layout_link_device_validate_otp.*
import kotlinx.android.synthetic.main.wtransactions_activity.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class LinkDeviceOTPFragment : Fragment(), View.OnClickListener {

    private var otpNumber: String? = null

    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            when {
                linkDeviceOTPEdtTxt5.isFocused -> {
                    if (TextUtils.isEmpty(linkDeviceOTPEdtTxt5.text)) {
                        disableNextButton()
                        linkDeviceOTPEdtTxt4.requestFocus()
                    } else {
                        enableNextButton()
                    }
                }
                linkDeviceOTPEdtTxt4.isFocused -> {
                    if(TextUtils.isEmpty(linkDeviceOTPEdtTxt4.text)){
                        linkDeviceOTPEdtTxt3.requestFocus()
                    } else {
                        linkDeviceOTPEdtTxt5.requestFocus()
                    }
                }
                linkDeviceOTPEdtTxt3.isFocused -> {
                    if(TextUtils.isEmpty(linkDeviceOTPEdtTxt3.text)) {
                        linkDeviceOTPEdtTxt2.requestFocus()
                    } else {
                        linkDeviceOTPEdtTxt4.requestFocus()

                    }

                }
                linkDeviceOTPEdtTxt2.isFocused -> {
                    if(TextUtils.isEmpty(linkDeviceOTPEdtTxt2.text)) {
                        linkDeviceOTPEdtTxt1.requestFocus()
                    } else {
                        linkDeviceOTPEdtTxt3.requestFocus()
                    }
                }
                linkDeviceOTPEdtTxt1.isFocused -> {
                    if (!TextUtils.isEmpty(linkDeviceOTPEdtTxt1?.text)) {
                        linkDeviceOTPEdtTxt2.requestFocus()
                    }
                }
            }
        }
    }


    private var mLinkDeviceOTPReq: Call<RetrieveOTPResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener("resendOTPType") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("type") as String
            // Do something with the result
            when {
                result.equals(OTPMethodType.SMS.name, true) -> {
                    callGetOTPAPI(OTPMethodType.SMS.name)
                }
                result.equals(OTPMethodType.EMAIL.name, true) -> {
                    callGetOTPAPI(OTPMethodType.EMAIL.name)
                }
                result.equals(OTP_CALL_CENTER, true) -> {
                    Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                }
                else -> {
                }
            }
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link_device_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()

        didNotReceiveOTPTextView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        didNotReceiveOTPTextView?.setOnClickListener(this)

        linkDeviceOTPEdtTxt1.addTextChangedListener(mTextWatcher)
        linkDeviceOTPEdtTxt2.addTextChangedListener(mTextWatcher)
        linkDeviceOTPEdtTxt3.addTextChangedListener(mTextWatcher)
        linkDeviceOTPEdtTxt4.addTextChangedListener(mTextWatcher)
        linkDeviceOTPEdtTxt5.addTextChangedListener(mTextWatcher)

        buttonNext.setOnClickListener(this)

        callGetOTPAPI(OTPMethodType.SMS.name)
    }

    private fun setToolbar() {
        activity?.apply {
            when (this) {
                is MyPreferencesInterface -> {
                    setToolbarTitle("")
                }
                is LinkDeviceConfirmationInterface -> {
                    setToolbarTitle("")
                    hideToolbarButton()
                }
            }
        }
    }

    private fun enableNextButton() {
        buttonNext?.isEnabled = true
        context?.let { buttonNext.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.next_button_icon)) }
    }

    private fun disableNextButton() {
        buttonNext?.isEnabled = false
        context?.let { buttonNext.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.next_button_inactive)) }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                LinkDeviceOTPFragment().apply {
                    arguments = Bundle().apply {
                    }
                }

        private const val TAG = "LinkDeviceOTPFragment"
        private const val OTP_CALL_CENTER = "CALL CENTER"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.didNotReceiveOTPTextView -> {
                view?.findNavController()?.navigate(R.id.action_linkDeviceOTPFragment_to_resendOTPBottomSheetFragment, bundleOf(
                        ResendOTPBottomSheetFragment.OTP_NUMBER to otpNumber
                ))
            }
            R.id.buttonNext -> {
                callValidatingOTPAPI()

                showErrorScreen(ErrorHandlerActivity.LINK_DEVICE_FAILED)
            }
        }
    }

    private fun callGetOTPAPI(otpMethod: String?) {
        mLinkDeviceOTPReq = otpMethod?.let { type -> OneAppService.getLinkDeviceOtp(type) }

        sendinOTPLayout?.visibility = View.VISIBLE
        linkDeviceOTPScreen?.visibility = View.GONE
        mLinkDeviceOTPReq?.enqueue(CompletionHandler(object : IResponseListener<RetrieveOTPResponse> {
            override fun onSuccess(retrieveOTPResponse: RetrieveOTPResponse?) {
                sendinOTPLayout?.visibility = View.GONE
                when (retrieveOTPResponse?.httpCode) {
                    200 -> {
                        Log.e(TAG, "response >> $retrieveOTPResponse")
                        linkDeviceOTPScreen?.visibility = View.VISIBLE
                        retrieveOTPResponse?.otpSentTo?.let {
                            if (otpMethod.equals(OTPMethodType.SMS.name, true)) {
                                otpNumber = it
                            }
                            enterOTPSubtitle?.text = activity?.resources?.getString(R.string.sent_otp_desc, it)
                            linkDeviceOTPEdtTxt1.requestFocus()
                        }
                    }
                    440 ->
                        activity?.apply {
                            if (!isFinishing) {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, retrieveOTPResponse?.response?.stsParams, this)
                            }
                        }
                    else -> retrieveOTPResponse?.response?.desc?.let { desc ->
                        try {
                            linkDeviceOTPScreen?.visibility = View.GONE
                        } catch (ex: IllegalStateException) {
                            FirebaseManager.logException(ex)
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                linkDeviceOTPScreen?.visibility = View.VISIBLE
            }
        }, RetrieveOTPResponse::class.java))

    }

    private fun callValidatingOTPAPI() {
        

    }

    private fun showErrorScreen(errorType: Int, errorMessage: String = "") {

        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE) {
            when (resultCode) {
                ErrorHandlerActivity.RESULT_RETRY -> {
                    Log.e(TAG, "RESULT_RETRY")
                }
                ErrorHandlerActivity.RESULT_CALL_CENTER -> {
                    Log.e(TAG, "RESULT_CALL_CENTER")
                }
            }
        }
    }
}
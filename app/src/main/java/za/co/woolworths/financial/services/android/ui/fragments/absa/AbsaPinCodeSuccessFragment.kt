package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.*
import com.android.volley.VolleyError
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_pin_code_complete_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaContentEncryptionRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaRegisterCredentialRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.absa.openbankingapi.woolworths.integration.dto.RegisterCredentialResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity.Companion.ERROR_PAGE_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.net.HttpCookie

class AbsaPinCodeSuccessFragment : Fragment() {

    private var mAliasId: String? = null
    private var fiveDigitPin: String? = null

    companion object {
        private const val FIVE_DIGIT_PIN_CODE = "FIVE_DIGIT_PIN_CODE"
        private const val ALIAS_ID = "ALIAS_ID"
        fun newInstance(aliasId: String?, fiveDigitPin: String) = AbsaPinCodeSuccessFragment().apply {
            arguments = Bundle(4).apply {
                putString(FIVE_DIGIT_PIN_CODE, fiveDigitPin)
                putString(ALIAS_ID, aliasId)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.absa_pin_code_complete_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        arguments?.apply {
            getString(FIVE_DIGIT_PIN_CODE)?.apply { fiveDigitPin = this }
            getString(ALIAS_ID)?.apply { mAliasId = this }
         }
    }


    private fun closeActivity() {
        activity?.apply {
            setResult(RESULT_OK)
            finish()
            overridePendingTransition(R.anim.stay, android.R.anim.fade_out)
        }
    }

    private fun initView() {
        gotItButton.setOnClickListener { navigateToAbsaLoginFragment() }
        registerCredentials(mAliasId,fiveDigitPin!!)
    }

    private fun registerCredentials(aliasId: String?, fiveDigitPin: String) {
        activity?.let {
            AbsaRegisterCredentialRequest().make(aliasId, fiveDigitPin,
                    object : AbsaBankingOpenApiResponse.ResponseDelegate<RegisterCredentialResponse> {

                        override fun onSuccess(response: RegisterCredentialResponse, cookies: List<HttpCookie>) {
                            Log.d("onSuccess", "onSuccess")
                            response.apply {
                                if (header?.resultMessages?.size == 0 || aliasId != null) {
                                    val absaSecureCredentials = AbsaSecureCredentials()
                                    absaSecureCredentials.aliasId = aliasId
                                    absaSecureCredentials.save()
                                    onRegistrationSuccess()
                                } else {
                                    showErrorScreen(ErrorHandlerActivity.WITH_NO_ACTION)
                                }
                            }

                        }

                        override fun onFailure(errorMessage: String) {
                            Log.d("onSuccess", "onFailure")
                            showErrorScreen(ErrorHandlerActivity.WITH_NO_ACTION)
                        }

                        override fun onFatalError(error: VolleyError?) {
                            showErrorScreen(ErrorHandlerActivity.WITH_NO_ACTION)
                        }
                    })
        }
    }

    fun onRegistrationSuccess() {
        AbsaContentEncryptionRequest.clearContentEncryptionData()
        val name = SessionUtilities.getInstance().jwt?.name?.get(0)
        tvTitle.text = getString(R.string.absa_success_title, name)
        tvDescription.text = resources.getString(R.string.absa_registration_success_desc)
        gotItButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        ivAppLogo.visibility = View.VISIBLE
    }

    private fun navigateToAbsaLoginFragment() {
        replaceFragment(
                fragment = AbsaLoginFragment.newInstance(),
                tag = AbsaLoginFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    /*private fun failureHandler(message: String?) {
        cancelRequest()
        view?.postDelayed({ message?.let { tapAndDismissErrorDialog(it) } }, 200)
    }*/

    private fun showErrorScreen(errorType: Int,errorMessage:String = "") {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            it.startActivityForResult(intent, ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.getItem(0)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

}
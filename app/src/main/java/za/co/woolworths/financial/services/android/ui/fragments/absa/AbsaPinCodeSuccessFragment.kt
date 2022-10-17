package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_pin_code_complete_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaContentEncryptionRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity.Companion.ERROR_PAGE_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel.AbsaIntegrationViewModel
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.OneAppEvents
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class AbsaPinCodeSuccessFragment : AbsaFragmentExtension() {

    private var mCreditCardNumber: String? = null
    private var mAliasId: String? = null
    private var fiveDigitPin: String? = null

    private val mViewModel: AbsaIntegrationViewModel by viewModels()

    companion object {
        private const val FIVE_DIGIT_PIN_CODE = "FIVE_DIGIT_PIN_CODE"
        private const val ALIAS_ID = "ALIAS_ID"
        fun newInstance(aliasId: String?, fiveDigitPin: String, creditAccountInfo: String?) = AbsaPinCodeSuccessFragment().apply {
            arguments = Bundle(4).apply {
                putString(FIVE_DIGIT_PIN_CODE, fiveDigitPin)
                putString(ALIAS_ID, aliasId)
                putString("creditCardToken", creditAccountInfo)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.absa_pin_code_complete_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).setPageTitle(getString(R.string.absa_registration_title_step_1)) }
        observeAbsaResult()
    }

    private fun observeAbsaResult() {
        with(mViewModel) {
            registerCredentialResponse.observe(viewLifecycleOwner) {
                val absaSecureCredentials = AbsaSecureCredentials()
                absaSecureCredentials.aliasId = mAliasId
                absaSecureCredentials.save()
                onRegistrationSuccess()
            }

            failureHandler.observe(viewLifecycleOwner) { onFailure ->
                showErrorScreen(ErrorHandlerActivity.COMMON)
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                when (isLoading) {
                    true -> showProgress()
                    false -> onProgressComplete()
                    else -> {}
                }
            }
        }
    }

    private fun onProgressComplete() {
        progressBar?.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        alwaysHideWindowSoftInputMode()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        arguments?.apply {
            getString(FIVE_DIGIT_PIN_CODE)?.apply { fiveDigitPin = this }
            getString(ALIAS_ID)?.apply { mAliasId = this }
            if (containsKey("creditCardToken")) {
                mCreditCardNumber = getString("creditCardToken") ?: ""
            }
        }
    }

    private fun initView() {
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).clearPageTitle() }
        gotItButton.setOnClickListener { navigateToAbsaLoginFragment() }
        registerCredentials(mAliasId, fiveDigitPin)
    }

    private fun registerCredentials(aliasId: String?, fiveDigitPin: String?) {
        showProgress()
        mViewModel.fetchRegisterCredentials(aliasId, fiveDigitPin)
    }

    private fun onRegistrationSuccess() {
        KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_REGISTRATION_SUCCESS, OneAppEvents.FeatureName.ABSA)
        AbsaContentEncryptionRequest.clearContentEncryptionData()
        val name = SessionUtilities.getInstance().jwt?.name?.get(0)
        tvTitle?.text = getString(R.string.absa_success_title, name)
        tvDescription?.text = resources.getString(R.string.absa_registration_success_desc)
        gotItButton?.visibility = View.VISIBLE
        ivAppLogo?.visibility = View.VISIBLE
        mViewModel.inProgress(false)
    }

    private fun navigateToAbsaLoginFragment() {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ABSA_CC_LOGIN_WITH_NEW_PASSCODE, this) }
        replaceFragment(
                fragment = AbsaLoginFragment.newInstance(mCreditCardNumber),
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

    private fun showErrorScreen(errorType: Int, errorMessage: String = "") {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            it.startActivityForResult(intent, ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.getItem(0)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showProgress() {
        tvTitle?.text = resources.getString(R.string.processing_your_request)
        tvDescription?.text = resources.getString(R.string.absa_registration_in_progress_desc)
        progressBar?.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ERROR_PAGE_REQUEST_CODE) {
            when (resultCode) {
                ErrorHandlerActivity.RESULT_RETRY -> {
                    registerCredentials(mAliasId, fiveDigitPin!!)
                }
            }
        }
    }

}
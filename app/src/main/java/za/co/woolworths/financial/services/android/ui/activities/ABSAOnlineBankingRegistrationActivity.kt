package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaOnlineBankingToDeviceActivityBinding
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.absa.*
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel.AbsaIntegrationViewModel
import za.co.woolworths.financial.services.android.util.Utils

class ABSAOnlineBankingRegistrationActivity : AppCompatActivity(), IDialogListener {

    private lateinit var binding: AbsaOnlineBankingToDeviceActivityBinding
    private var mAccounts: String? = null
    private var mShouldDisplayABSALogin: Boolean? = false
    private var mCreditAccountInfo: String? = ""

    private val viewModel: AbsaIntegrationViewModel? by viewModels()

    companion object {
        const val SHOULD_DISPLAY_LOGIN_SCREEN = "SHOULD_DISPLAY_LOGIN_SCREEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AbsaOnlineBankingToDeviceActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)

        actionBar()
        if (savedInstanceState == null) {
            getBundleArgument()
            if (mShouldDisplayABSALogin!!) {
                addFragment(
                        fragment = AbsaLoginFragment.newInstance(mCreditAccountInfo),
                        tag = AbsaLoginFragment::class.java.simpleName,
                        containerViewId = R.id.flAbsaOnlineBankingToDevice)
            } else {
                addFragment(
                        fragment = AbsaBoardingFragment.newInstance(mCreditAccountInfo),
                        tag = AbsaBoardingFragment::class.java.simpleName,
                        containerViewId = R.id.flAbsaOnlineBankingToDevice)
            }
        }
    }

    private fun getBundleArgument() {

        var bundle :Bundle ?=intent?.extras
        var message = bundle!!.getString("value")

        intent?.extras?.apply {
            mShouldDisplayABSALogin = getBoolean(SHOULD_DISPLAY_LOGIN_SCREEN, false)
            mAccounts = bundle.getString(ChatFragment.ACCOUNTS, "")
            mCreditAccountInfo = getString("creditCardToken")
        }
    }

    private fun actionBar() {
        setSupportActionBar(binding.tbOnlineBanking)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home, R.id.itmIconClose -> {
                navigateBack()
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {

        when(getCurrentFragment()){
            is AbsaConfirmFiveDigitCodeFragment -> {
                //Hide back button when moving back to EnterFiveDigitPassCode screen
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            is AbsaOTPConfirmationFragment -> {
                viewModel?.clearAliasIdAndCellphoneNumber()
            }

            is AbsaPinCodeSuccessFragment, is AbsaLoginFragment -> {
                // Refrain from navigate to previous fragment when landing fragment is AbsaPinCodeSuccessFragment
                finishActivity()
            }

            is AbsaFiveDigitCodeFragment -> {
                closeDownActivity()
            }

            is AbsaSecurityCheckSuccessfulFragment -> return
            
            is AbsaEnterAtmPinCodeFragment -> {
                Utils.hideSoftKeyboard(this)
            }
        }


        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            finishActivity()
        }
    }

     fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager?.findFragmentById(R.id.flAbsaOnlineBankingToDevice)
    }

    override fun onDialogDismissed() {
        Handler().postDelayed({ finishActivity() }, 200)
    }

    private fun closeDownActivity() {
        this.apply {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.absa_close_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            closeDownActivity()
        } else if (requestCode == ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE && resultCode == ErrorHandlerActivity.RESULT_RESET_PASSCODE) {
            startAbsaRegistration()
        } else {
            getCurrentFragment()?.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun startAbsaRegistration() {
        replaceFragmentSafely(
                fragment = AbsaEnterAtmPinCodeFragment.newInstance(mCreditAccountInfo),
                tag = AbsaEnterAtmPinCodeFragment::class.java.simpleName, allowStateLoss = false, allowBackStack = false,
                containerViewId = R.id.flAbsaOnlineBankingToDevice)
    }

    fun setPageTitle(title: String) {
        binding.toolbarText.text = title
    }

    fun clearPageTitle(){
        binding.toolbarText.text = ""
    }
}
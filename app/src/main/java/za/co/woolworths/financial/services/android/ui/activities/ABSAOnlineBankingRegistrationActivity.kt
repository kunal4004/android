package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_online_banking_to_device_activity.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.absa.*
import za.co.woolworths.financial.services.android.util.Utils

class ABSAOnlineBankingRegistrationActivity : AppCompatActivity(), IDialogListener {

    private var mShouldDisplayABSALogin: Boolean? = false
    private var mCreditAccountInfo: String? = ""

    companion object {
        const val SHOULD_DISPLAY_LOGIN_SCREEN = "SHOULD_DISPLAY_LOGIN_SCREEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.absa_online_banking_to_device_activity)
        Utils.updateStatusBarBackground(this)

        actionBar()
        if (savedInstanceState == null) {
            getBundleArgument()
            if (mShouldDisplayABSALogin!!) {
                addFragment(
                        fragment = AbsaLoginFragment.newInstance(),
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
            mCreditAccountInfo = getString("creditCardToken")
        }
    }

    private fun actionBar() {
        setSupportActionBar(tbOnlineBanking)
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

        //Hide back button when moving back to EnterFiveDigitPassCode screen
        if (getCurrentFragment() is AbsaConfirmFiveDigitCodeFragment)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Refrain from navigate to previous fragment when landing fragment is AbsaPinCodeSuccessFragment
        if ((getCurrentFragment() is AbsaPinCodeSuccessFragment) || (getCurrentFragment() is AbsaLoginFragment)) {
            finishActivity()
            return
        }

        if ((getCurrentFragment() is AbsaFiveDigitCodeFragment)) {
            closeDownActivity()
            return
        }

        if ((getCurrentFragment() is AbsaEnterAtmPinCodeFragment)) {
            Utils.hideSoftKeyboard(this)
        }

        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            finishActivity()
        }
    }

    public fun finishActivity() {
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

    override fun onDialogButtonAction() {

    }

    fun startAbsaRegistration() {
        replaceFragmentSafely(
                fragment = AbsaEnterAtmPinCodeFragment.newInstance(mCreditAccountInfo),
                tag = AbsaEnterAtmPinCodeFragment::class.java.simpleName, allowStateLoss = false, allowBackStack = false,
                containerViewId = R.id.flAbsaOnlineBankingToDevice)
    }

    fun setPageTitle(title: String) {
        toolbarText.text = title
    }

    fun clearPageTitle(){
        toolbarText.text = ""
    }
}
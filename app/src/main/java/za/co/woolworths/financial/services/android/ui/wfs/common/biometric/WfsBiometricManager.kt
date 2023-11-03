package za.co.woolworths.financial.services.android.ui.wfs.common.biometric

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricUtils.deviceHasPasswordPinLock
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricUtils.isBiometricHardWareAvailable
import za.co.woolworths.financial.services.android.ui.wfs.common.state.BiometricSingleton
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.SessionUtilities.*
import java.util.concurrent.Executor
import javax.inject.Inject

enum class BiometricCallback { Error, Succeeded, Failed, ErrorUserCanceled }

interface WfsBiometricManager {
    fun isBiometricEnabled(context: Context): Boolean
    fun isBiometricInMyPreferencesEnabled(context: Context): Boolean

    fun setupBiometricAuthenticationForAccountLanding(
        biometricSingleton: BiometricSingleton?,
        fragment: Fragment,
        appCompatActivity: AppCompatActivity?,
        viewModel: UserAccountLandingViewModel
    )

    fun setupBiometricInWRewardsVouchersOnItemTap(
        fragment: Fragment,
        bottomNavigation: WBottomNavigationView?,
        callback: (BiometricCallback) -> Unit
    )

    fun setupBiometricAuthenticationForBottomNavigation(
        bottomNavigation: BottomNavigationActivity?,
        callback: (BiometricCallback) -> Unit
    )


    fun show()
}

class WfsBiometricManagerImpl @Inject constructor() : WfsBiometricManager {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun isBiometricEnabled(context: Context): Boolean {
        return getInstance().isUserAuthenticated
                && AuthenticateUtils.isBiometricAuthenticationAvailable(context)
    }

    override fun isBiometricInMyPreferencesEnabled(context: Context): Boolean {
        return getInstance().isUserAuthenticated
                && AuthenticateUtils.isBiometricAuthenticationSupported(context)
    }

    private fun Fragment.setPrompt(callback: (BiometricCallback) -> Unit) {
        executor = ContextCompat.getMainExecutor(this.requireContext())
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            biometricPromptAuthenticationCallback { result -> callback(result) })
    }
    private fun AppCompatActivity.setPrompt(callback: (BiometricCallback) -> Unit) {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, biometricPromptAuthenticationCallback { result -> callback(result) })
    }
    private fun biometricPromptAuthenticationCallback(callback: (BiometricCallback) -> Unit): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    callback(BiometricCallback.ErrorUserCanceled)
                } else {
                    callback(BiometricCallback.Error)
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callback(BiometricCallback.Succeeded)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callback(BiometricCallback.Failed)
            }
        }
    }

    private fun initBiometricPrompt(
        title: String,
        subtitle: String,
        description: String,
        isDeviceCredentialsAvailable: Boolean
    ) {
        val authFlag = BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG
        if (isDeviceCredentialsAvailable) {
            /*For API level > 30
              Newer API setAllowedAuthenticators is used*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDeviceCredentialAllowed(true)
                    .setDescription(description)
                    .setAllowedAuthenticators(authFlag)
                    .build()
            } else {
                /*SetDeviceCredentials method deprecation is ignored here
                  as this block is for API level<30*/
                @Suppress("DEPRECATION")
                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setDeviceCredentialAllowed(true)
                    .build()
            }
        } else {
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setDeviceCredentialAllowed(true)
                .build()
        }
    }

    override fun show() {
        biometricPrompt.authenticate(promptInfo)
    }

    private fun Context.unlockWithPinPasswordPatternOrBiometric() : Boolean {
        return if (AuthenticateUtils.isDeviceSecure(context = this)) {
            val title = getString(R.string.enter_password)
            if (isBiometricHardWareAvailable()) {
                initBiometricPrompt(
                    title,
                    Constants.BIOMETRIC_AUTHENTICATION_SUBTITLE,
                    Constants.BIOMETRIC_AUTHENTICATION_DESCRIPTION,
                    isDeviceCredentialsAvailable = false
                )
            } else {
                if (deviceHasPasswordPinLock()) {
                    initBiometricPrompt(
                        title,
                        Constants.BIOMETRIC_AUTHENTICATION_SUBTITLE,
                        Constants.BIOMETRIC_AUTHENTICATION_DESCRIPTION,
                        isDeviceCredentialsAvailable = true
                    )
                } else {
                     false
                }
            }
            true
        } else {
            false
        }
    }

    override fun setupBiometricAuthenticationForAccountLanding(
        biometricSingleton: BiometricSingleton?,
        fragment: Fragment,
        appCompatActivity: AppCompatActivity?,
        viewModel: UserAccountLandingViewModel
    ) {
        with(fragment) {
            if (isAdded &&
                isVisible &&
                isBiometricEnabled(fragment.requireContext())) {
                appCompatActivity?.apply {
                    when (unlockWithPinPasswordPatternOrBiometric()){
                        true -> {
                            biometricSingleton?.wasBiometricAuthenticationScreenActive = true
                            fragment.setPrompt { callback ->
                            when (callback) {
                                BiometricCallback.ErrorUserCanceled -> {
                                    when (appCompatActivity) {
                                        is BottomNavigationActivity -> { appCompatActivity.bottomNavigationById?.currentItem = BottomNavigationActivity.INDEX_TODAY }
                                        is MyAccountActivity -> Unit
                                    }
                                    viewModel.setScreenBlurDisabled()
                                    viewModel.setBiometricEnabled()
                                    biometricSingleton?.wasBiometricAuthenticationScreenActive = false
                                }

                                BiometricCallback.Succeeded ->  {
                                    viewModel.setOnTapNotActivated()
                                    viewModel.setScreenBlurDisabled()
                                    viewModel.setBiometricDisabled()
                                    biometricSingleton?.wasBiometricAuthenticationScreenActive = false
                                }
                                else -> Unit
                            }
                        }
                        }
                        false -> {
                            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                            startActivity(intent)
                        }
                    }
                    viewModel.setScreenBlurEnabled()
                    show()
                }
            }else {
                viewModel.setScreenBlurDisabled()
            }
        }
    }

    override fun setupBiometricInWRewardsVouchersOnItemTap(
        fragment: Fragment,
        bottomNavigation: WBottomNavigationView?,
        callback: (BiometricCallback) -> Unit
    ) {
        with(fragment) {
            if (isAdded && isVisible) {
                (requireActivity() as? AppCompatActivity)?.apply {
                    when(unlockWithPinPasswordPatternOrBiometric()) {
                        true -> fragment.setPrompt { result -> callback(result) }
                        false -> Unit
                    }
                }
            }
        }
    }

    override fun setupBiometricAuthenticationForBottomNavigation(
        bottomNavigation: BottomNavigationActivity?,
        callback: (BiometricCallback) -> Unit) {
        bottomNavigation?.apply {
            when (unlockWithPinPasswordPatternOrBiometric()) {
                true -> { setPrompt { result -> callback(result) }}
                false -> Unit
            }
        }
    }

}
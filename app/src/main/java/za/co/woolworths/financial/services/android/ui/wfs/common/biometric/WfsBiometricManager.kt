package za.co.woolworths.financial.services.android.ui.wfs.common.biometric

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricUtils.deviceHasPasswordPinLock
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricUtils.isBiometricHardWareAvailable
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities.*
import java.util.concurrent.Executor
import javax.inject.Inject

enum class BiometricCallback { Error, Succeeded, Failed, ErrorUserCanceled }

interface WfsBiometricManager {
    var isFragmentObscuredByOverlay: Boolean
    fun isBiometricEnabled() : Boolean
    fun setupBiometricAuthenticationForAccountLanding(
        fragment: Fragment,
        bottomNavigation: WBottomNavigationView?,
        viewModel: UserAccountLandingViewModel)
    fun setupBiometricInWRewardsVouchersOnItemTap(
        fragment: Fragment,
        bottomNavigation: WBottomNavigationView?,
        callback: (BiometricCallback) -> Unit)
    fun isFragmentObscuredByOverlay(isHidden : Boolean)
    fun show()
}

class WfsBiometricManagerImpl @Inject constructor() : WfsBiometricManager {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override var isFragmentObscuredByOverlay: Boolean = false
    override fun isBiometricEnabled(): Boolean {
        return getInstance().isUserAuthenticated
                && AuthenticateUtils.instance.isBiometricAuthenticationRequired
    }

    private fun Fragment.setPrompt(callback: (BiometricCallback) -> Unit) {
        executor = ContextCompat.getMainExecutor(this.requireContext())
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

    private fun Fragment.isDeviceSecure(): Boolean {
        val keyguardManager = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardSecure
    }

    private fun initBiometricPrompt(
        title: String,
        subtitle: String,
        description: String,
        isDeviceCredentialsAvailable: Boolean
    ) {
        if (isDeviceCredentialsAvailable) {
            /*For API level > 30
              Newer API setAllowedAuthenticators is used*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val authFlag = DEVICE_CREDENTIAL or BIOMETRIC_STRONG
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

    override fun isFragmentObscuredByOverlay(isHidden: Boolean) {
                this.isFragmentObscuredByOverlay = isHidden
    }

    private fun Fragment.unlockWithPinPasswordPatternOrBiometric(callback: (BiometricCallback) -> Unit) {
        if (isDeviceSecure()) {
            val title= getString(R.string.enter_password)
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
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    startActivity(intent)
                    return
                }
            }
            this.setPrompt(callback = callback)
        }else {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            startActivity(intent)
        }
    }

    override fun setupBiometricAuthenticationForAccountLanding(
        fragment: Fragment,
        bottomNavigation: WBottomNavigationView?,
        viewModel: UserAccountLandingViewModel) {
        with(fragment) {
            if (isAdded && isVisible && isFragmentObscuredByOverlay.not()) {
                viewModel.enableBiometricBlur()
                (requireActivity() as? AppCompatActivity)?.apply {
                    unlockWithPinPasswordPatternOrBiometric { callback ->
                        when (callback) {
                            BiometricCallback.ErrorUserCanceled -> {
                                bottomNavigation?.currentItem = BottomNavigationActivity.INDEX_TODAY
                                viewModel.disableBiometricBlur()
                                AuthenticateUtils.getInstance(this)
                                    .enableBiometricForCurrentSession(true)
                            }

                            BiometricCallback.Succeeded -> viewModel.disableBiometricBlur()

                            else -> Unit
                        }
                    }
                    show()
                }
            }
            isFragmentObscuredByOverlay(isHidden = false)
            }
        }

    override fun setupBiometricInWRewardsVouchersOnItemTap(
        fragment: Fragment,
        bottomNavigation: WBottomNavigationView?,
        callback: (BiometricCallback) -> Unit) {
        with(fragment) {
            if (isAdded && isVisible && isFragmentObscuredByOverlay.not()) {
                (requireActivity() as? AppCompatActivity)?.apply {
                    unlockWithPinPasswordPatternOrBiometric { result ->
                        callback(result)
                    }
                }
            }
            isFragmentObscuredByOverlay(isHidden = false)
        }
    }
}
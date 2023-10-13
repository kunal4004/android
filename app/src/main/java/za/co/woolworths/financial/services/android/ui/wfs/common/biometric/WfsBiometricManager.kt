package za.co.woolworths.financial.services.android.ui.wfs.common.biometric

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricUtils.deviceHasPasswordPinLock
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricUtils.isBiometricHardWareAvailable
import java.util.concurrent.Executor
import javax.inject.Inject

enum class BiometricCallback { Error, Succeeded, Failed, ErrorUserCanceled }

interface WfsBiometricManager {

    var isFragmentObscuredByOverlay: Boolean
    var isFragmentFirstTimeVisible : Boolean
    fun isFragmentObscuredByOverlay(isHidden : Boolean)
    fun Fragment.configureBiometricResult(prompt : (BiometricCallback) -> Unit)
    fun show()
}

class WfsBiometricManagerImpl @Inject constructor() : WfsBiometricManager {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override var isFragmentObscuredByOverlay: Boolean = false
    override var isFragmentFirstTimeVisible : Boolean = false

    private fun Fragment.setPrompt(callback: (BiometricCallback) -> Unit) {
        executor = ContextCompat.getMainExecutor(this.requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        callback(BiometricCallback.ErrorUserCanceled)
                    }else {
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
            })
    }

    private fun Fragment.isDeviceSecure(): Boolean {
        val keyguardManager = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
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


    override fun Fragment.configureBiometricResult(prompt: (BiometricCallback) -> Unit) {
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
            setPrompt(callback = prompt)
        }else {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            startActivity(intent)
        }
    }
}
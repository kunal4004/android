package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_biometrics

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.ui.activities.BiometricsWalkthrough
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import javax.inject.Inject

interface Biometrics {
    fun appInstanceObject(): AppInstanceObject?
}

class BiometricImpl @Inject constructor()  : Biometrics {

    override fun appInstanceObject(): AppInstanceObject? {
        return AppInstanceObject.get()
    }
}

interface BiometricActivityResult {
    fun createBiometricIntent() : Intent
    fun registerBiometricForResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
}

class BiometricActivityResultImpl @Inject constructor(private val activity: Activity) : BiometricActivityResult {

    override fun createBiometricIntent(): Intent {
        return Intent(activity, BiometricsWalkthrough::class.java)
    }

    override fun registerBiometricForResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        val biometricIntent = createBiometricIntent()
        activityLauncher?.launch(biometricIntent, onActivityResult = {})
    }
}
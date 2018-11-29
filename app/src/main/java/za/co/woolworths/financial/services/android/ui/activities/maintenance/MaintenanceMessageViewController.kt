package za.co.woolworths.financial.services.android.ui.activities.maintenance

import android.content.Intent
import android.os.Handler
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import android.os.Looper

open class MaintenanceMessageViewController {

    fun presentModal() {
        val appInstance = WoolworthsApplication.getInstance() ?: return
        Handler(Looper.getMainLooper()).post {
            val navigateToRuntimeActivity = Intent(appInstance, MaintenanceMessageActivity::class.java)
            navigateToRuntimeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            navigateToRuntimeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appInstance.startActivity(navigateToRuntimeActivity)
        }
    }
}
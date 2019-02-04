package za.co.woolworths.financial.services.android.ui.activities.maintenance

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

open class MaintenanceMessageViewController(name: String?) : HandlerThread(name) {

    var handler: Handler? = null

    fun openActivity() {
        start()
        handler = Handler(looper)
        handler?.post {
            val appInstance = WoolworthsApplication.getInstance() ?: return@post
            val navigateToRuntimeActivity = Intent(appInstance, MaintenanceMessageActivity::class.java)
            navigateToRuntimeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            navigateToRuntimeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appInstance.startActivity(navigateToRuntimeActivity)
            quit()
        }
        handler?.sendEmptyMessage(0)
    }
}
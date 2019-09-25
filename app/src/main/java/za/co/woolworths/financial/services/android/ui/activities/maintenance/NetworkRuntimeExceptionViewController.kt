package za.co.woolworths.financial.services.android.ui.activities.maintenance

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorMessageDialogWithTitleFragment
import androidx.fragment.app.FragmentActivity

open class NetworkRuntimeExceptionViewController : HandlerThread(NetworkRuntimeExceptionViewController::class.java.simpleName) {

    var handler: Handler? = null

    fun openMaintenanceView() {
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

    fun openSocketTimeOutDialog() {
        start()
        handler = Handler(looper)
        handler!!.post {
            val appInstance = (WoolworthsApplication.getInstance()?.currentActivity as? FragmentActivity)
                    ?: return@post

            val fragment = appInstance.supportFragmentManager.findFragmentByTag(ErrorMessageDialogWithTitleFragment::class.java.simpleName)
            val socketTimeoutExceptionDialog = ErrorMessageDialogWithTitleFragment.newInstance()
            if (fragment is ErrorMessageDialogWithTitleFragment) return@post
                socketTimeoutExceptionDialog.show(appInstance.supportFragmentManager.beginTransaction(), ErrorMessageDialogWithTitleFragment::class.java.simpleName)

            quit()
        }
        handler?.sendEmptyMessage(0)
    }


}
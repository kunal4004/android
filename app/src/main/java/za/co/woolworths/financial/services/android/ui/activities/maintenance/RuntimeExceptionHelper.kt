package za.co.woolworths.financial.services.android.ui.activities.maintenance

import android.content.Intent
import android.util.Log
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.Utils

open class RuntimeExceptionHelper {

    fun navigateToRuntimeExceptionActivity() {
        val woolworthsApplication = WoolworthsApplication.getInstance() ?: return
        val navigateToRuntimeActivity = Intent(woolworthsApplication, RuntimeExceptionActivity::class.java)
        navigateToRuntimeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        woolworthsApplication.startActivity(navigateToRuntimeActivity)
    }

    fun <Result> getHttpCode(result: Result): String? {
        try {
            val obj = JSONObject(Utils.toJson(result))
            if (obj.has("httpCode")) {
                return obj.getString("httpCode")
            }
        } catch (t: Throwable) {
            Log.d("runtimeException", t.message)
        }
        return ""
    }
}
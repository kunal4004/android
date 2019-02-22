package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import android.app.Activity
import android.content.Intent
import com.google.gson.Gson

class ShoppingListToastNavigation {
    companion object {
        const val DISPLAY_TOAST_RESULT_CODE = 120
        fun requestToastOnNavigateBack(activity: Activity?, key: String, value: Any?) {
            activity?.apply {
                val output = Intent()
                output.putExtra(key, Gson().toJson(value))
                setResult(Activity.RESULT_OK, output)
                finish()
                overridePendingTransition(0, 0)
            }
        }

        fun displayBottomNavigationToast(activity: Activity?, key: String, value: Any?) {
            activity?.apply {
                val output = Intent()
                output.putExtra(key, Gson().toJson(value))
                setResult(DISPLAY_TOAST_RESULT_CODE, output)
                finish()
                overridePendingTransition(0, 0)
            }

        }
    }
}
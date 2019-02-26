package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity

class NavigateToShoppingList {
    companion object {
        const val DISPLAY_TOAST_RESULT_CODE = 120
        fun requestToastOnNavigateBack(activity: Activity?, key: String, value: Any?) {
            activity?.apply {
                val output = Intent()
                output.putExtra(key, Gson().toJson(value))
                setResult(ADD_TO_SHOPPING_LIST_RESULT_CODE, output)
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

    fun openShoppingList(activity: Activity?, addToListRequest: Any?, orderId: String?, navigateToCreateList: Boolean) {
        activity?.apply {
            val intentAddToList = Intent(this, AddToShoppingListActivity::class.java)
            intentAddToList.putExtra("addToListRequest", Gson().toJson(addToListRequest))
            intentAddToList.putExtra("shouldDisplayCreateList", navigateToCreateList)
            intentAddToList.putExtra(OrderDetailsActivity.ORDER_ID, orderId ?: "")
            startActivityForResult(intentAddToList, AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE)
            overridePendingTransition(0, 0)
        }
    }
}
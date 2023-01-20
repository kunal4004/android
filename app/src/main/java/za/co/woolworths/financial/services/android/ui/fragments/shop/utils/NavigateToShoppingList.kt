package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonElement
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ScreenManager

class NavigateToShoppingList {
    companion object {
        const val DISPLAY_TOAST_RESULT_CODE = 120
        fun requestToastOnNavigateBack(activity: Activity?, key: String, value: Any?) {
            activity?.apply {
                val sizeOfList = value as? Map<*, *>
                val output = Intent()
                output.putExtra(key, Gson().toJson(value))
                output.putExtra("sizeOfList", sizeOfList?.size)
                setResult(ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, output)
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

        fun navigateToShoppingListOnToastClicked(activity: Activity?, jsonElement: JsonElement) {
            jsonElement.asJsonObject?.apply {
                if (size() == 1) {
                    var listName = ""
                    var listId = ""
                    for (listItem in entrySet()) {
                        val item = listItem.value.asJsonObject
                        listId = item.get("id").asString
                        listName = item.get("name").asString
                    }
                    ScreenManager.presentShoppingListDetailActivity(activity, listId, listName)
                }
            }
        }

        fun openShoppingList(
            activity: Activity?,
            addToListRequest: Any?,
            orderId: String?,
            navigateToCreateList: Boolean,
        ) {
            activity?.apply {
                val intentAddToList = Intent(this, AddToShoppingListActivity::class.java)
                intentAddToList.putExtra("addToListRequest", Gson().toJson(addToListRequest))
                intentAddToList.putExtra("shouldDisplayCreateList", navigateToCreateList)
                intentAddToList.putExtra(AppConstant.ORDER_ID, orderId ?: "")
                startActivityForResult(intentAddToList,
                    AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE)
                overridePendingTransition(0, 0)
            }
        }
    }
}
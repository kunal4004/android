package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import android.app.Activity
import com.google.gson.JsonElement
import za.co.woolworths.financial.services.android.util.ScreenManager

class NavigateToShoppingList {
    companion object {

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
    }
}
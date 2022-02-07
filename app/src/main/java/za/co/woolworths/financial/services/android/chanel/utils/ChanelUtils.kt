package za.co.woolworths.financial.services.android.chanel.utils

import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.brandLandingPage

class ChanelUtils {

    companion object {
        fun isCategoryPresentInConfig(categoryName: String?): Boolean {
            val brandLandingPage = brandLandingPage
            if (brandLandingPage == null || !brandLandingPage.isEnabled) {
                return false
            }
            for (categoryNameFromList in brandLandingPage.categoryName) {
                if (!TextUtils.isEmpty(categoryName) && categoryName.equals(
                        categoryNameFromList,
                        ignoreCase = true
                    )
                ) {
                    return true
                }
            }
            return false
        }
    }
}
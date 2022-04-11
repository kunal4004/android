package za.co.woolworths.financial.services.android.chanel.utils

import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.brandLandingPage
import za.co.woolworths.financial.services.android.models.dto.app_config.BrandCategory

class ChanelUtils {

    companion object {
        fun isCategoryPresentInConfig(categoryName: String?): Boolean {
            val brandLandingPage = brandLandingPage
            if (brandLandingPage == null || !brandLandingPage.isEnabled) {
                return false
            }
            for (brandCategory in brandLandingPage.brandCategories) {
                if (!TextUtils.isEmpty(categoryName) && categoryName.equals(
                        brandCategory.brandName,
                        ignoreCase = true
                    )
                ) {
                    return true
                }
            }
            return false
        }

        fun getBrandCategory(categoryName: String?): BrandCategory? {
            if (TextUtils.isEmpty(categoryName)) {
                return null
            }
            for (brandCategory in brandLandingPage?.brandCategories ?: ArrayList(0)) {
                if (categoryName.equals(brandCategory.brandName, ignoreCase = true)) {
                    return brandCategory
                }
            }
            return null
        }
    }
}
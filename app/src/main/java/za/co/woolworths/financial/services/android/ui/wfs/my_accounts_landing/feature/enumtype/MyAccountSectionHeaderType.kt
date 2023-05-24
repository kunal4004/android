package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.enumtype

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem

enum class MyAccountSectionHeaderType  {
    MyProducts,
    MyOffers,
    MyProfile,
    General;

    fun title() = when (this) {
        MyProducts -> CommonItem.Header(title = R.string.my_products, automationLocatorKey = AutomationTestScreenLocator.my_product_key)
        MyOffers -> CommonItem.Header(title = R.string.my_offers_label,automationLocatorKey = AutomationTestScreenLocator.my_offer_key)
        MyProfile -> CommonItem.Header(title =R.string.acc_my_profile,automationLocatorKey = AutomationTestScreenLocator.my_profile_key)
        General -> CommonItem.Header(title =R.string.txt_general,automationLocatorKey = AutomationTestScreenLocator.my_general_key)
    }
}
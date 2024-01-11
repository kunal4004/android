package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.stabletype

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.contact_us_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_detail_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_message_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_order_again_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_orders_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_preferences_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_shopping_lists
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.need_help_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.store_locator_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.update_password_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.enumtype.MyAccountSectionHeaderType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.General
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.MyProfile

sealed class GeneralProductType {
    data class StoreLocator(
        val general: CommonItem.General = CommonItem.General(
            icon = R.drawable.ic_store_locator,
            title = R.string.store_locator,
            clickable = General.StoreLocator,
            automationLocatorKey = store_locator_key
        )) : GeneralProductType()
    data class NeedHelp(
        val general: CommonItem.General = CommonItem.General(
            icon = R.drawable.ic_need_help, title = R.string.need_help,
            clickable = General.NeedHelp,
            automationLocatorKey = need_help_key
        )) : GeneralProductType()

    data class ContactUs(
        val general: CommonItem.General = CommonItem.General(
            isShimmerDividedByTwo = true,
            icon = R.drawable.ic_contact_us,
            title = R.string.contact_us,
            clickable = General.ContactUs,
            automationLocatorKey = contact_us_key
        )) : GeneralProductType()
    data class UpdatePassword(
        val general: CommonItem.General = CommonItem.General(
            icon = R.drawable.ic_update_password,
            title = R.string.acc_update_password,
            clickable = General.UpdatePassword,
            automationLocatorKey = update_password_key
        )) : GeneralProductType()
    data class MyPreferences(
        val general: CommonItem.General = CommonItem.General(
            icon = R.drawable.ic_my_preferences,
            title = R.string.acc_my_preferences,
            clickable = General.Preferences,
            automationLocatorKey = my_preferences_key
        )) : GeneralProductType()
    data class SignOut(
        val general: CommonItem.General = CommonItem.General(
            isShimmerDividedByTwo = true,
            icon = R.drawable.ic_sign_out,
            title = R.string.sign_out,
            clickable = General.SignOut,
                    automationLocatorKey = sign_out_key

        )) : GeneralProductType()
    data class MyDetails(
        val general: CommonItem.General = CommonItem.General(
            isShimmerDividedByTwo = true,
            icon = R.drawable.ic_my_details,
            title = R.string.acc_my_detail,
            clickable = MyProfile.Detail,
            automationLocatorKey = my_detail_key
        )) : GeneralProductType()
    data class MyOrders(
        val general: CommonItem.General = CommonItem.General(
            isShimmerDividedByTwo = true,
            icon = R.drawable.ic_delivery_truck,
            title = R.string.my_orders_title,
            clickable = MyProfile.Order,
            automationLocatorKey = my_orders_key
        )) : GeneralProductType()
    data class OrderAgain(
        val general: CommonItem.General = CommonItem.General(
            isShimmerDividedByTwo = true,
            icon = R.drawable.ic_order_again,
            title = R.string.my_account_order_again,
            clickable = MyProfile.OrderAgain,
            automationLocatorKey = my_order_again_key
        )) : GeneralProductType()
    data class Messages(
        var unreadMessageCount: Int = 0, val general: CommonItem.General = CommonItem.General(
            icon = R.drawable.ic_messages,
            isShimmerDividedByTwo = true,
            title = R.string.messages,
            clickable = MyProfile.Message,
            automationLocatorKey = my_message_key
        )) : GeneralProductType()
    data class MyShoppingLists(
        val general: CommonItem.General = CommonItem.General(
            icon = R.drawable.ic_my_shopping_lists,
            title = R.string.my_shopping_lists,
            clickable = MyProfile.ShoppingList,
            automationLocatorKey = my_shopping_lists
        )) : GeneralProductType()
    companion object {
        fun list() = mutableListOf<Any>().apply {
            add(MyAccountSectionHeaderType.MyProfile.title())
            add(CommonItem.Spacer24dp)
            add(CommonItem.Divider)
            add(MyDetails())
            add(CommonItem.Divider)
            add(MyOrders())
            add(CommonItem.Divider)
            add(Messages())
            add(CommonItem.Divider)
            add(MyShoppingLists())
            add(CommonItem.Divider)
            add(OrderAgain())
            add(CommonItem.SectionDivider)
        }
        fun getGeneralItems(item : GeneralProductType) = when(item) {
            is Messages -> item.general
            is MyDetails -> item.general
            is MyOrders -> item.general
            is MyShoppingLists -> item.general
            is ContactUs -> item.general
            is MyPreferences -> item.general
            is NeedHelp -> item.general
            is SignOut -> item.general
            is StoreLocator -> item.general
            is UpdatePassword -> item.general
            is OrderAgain -> item.general
        }
        fun generalProduct() = mutableListOf(
            MyAccountSectionHeaderType.General.title(),
            CommonItem.Spacer24dp,
            CommonItem.Divider,
            StoreLocator(),
            CommonItem.Divider,
            NeedHelp(),
            CommonItem.Divider,
            ContactUs(),
            CommonItem.Divider,
            UpdatePassword(),
            CommonItem.Divider,
            MyPreferences(),
            CommonItem.Divider,
            SignOut()
        )
        fun loggedOutProduct() = mutableListOf(
            CommonItem.SectionDivider,
            MyAccountSectionHeaderType.General.title(),
            StoreLocator(),
            CommonItem.Divider,
            NeedHelp(),
            CommonItem.Divider,
            ContactUs()
        )
    }
}

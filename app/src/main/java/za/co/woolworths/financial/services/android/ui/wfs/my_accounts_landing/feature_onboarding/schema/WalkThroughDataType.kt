package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_onboarding.schema

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.page1_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.page2_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.page3_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.page4_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ParentAccountModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.SignedOut
import javax.annotation.concurrent.Immutable

sealed class  WalkThroughDataType {
    object Page1 : WalkThroughDataType()
    object Page2 : WalkThroughDataType()
    object Page3 : WalkThroughDataType()
    object Page4 : WalkThroughDataType()

    fun toWalkThrough() = when (this) {
        Page1 -> WalkThrough(
            stringId = R.string.on_boarding_screen_title_1,
            resourceId = R.drawable.accounts_walkthrough_welcome,
            automationTestScreenLocator = page1_key
        )
        Page2 -> WalkThrough(
            stringId = R.string.on_boarding_screen_title_2,
            resourceId = R.drawable.accounts_walkthrough_shop,
            automationTestScreenLocator = page2_key

        )
        Page3 -> WalkThrough(
            stringId = R.string.on_boarding_screen_title_3,
            resourceId = R.drawable.accounts_walkthrough_wrewards,
            automationTestScreenLocator = page3_key

        )
        Page4 -> WalkThrough(
            stringId = R.string.on_boarding_screen_title_4,
            resourceId = R.drawable.accounts_walkthrough_accounts,
            automationTestScreenLocator = page4_key
        )
    }

    companion object {
        fun list() = mutableListOf<ParentAccountModel>().apply {
            add(CommonItem.Spacer24dp)
            add(CommonItem.Toolbar(title = R.string.my_accounts))
            add(CommonItem.Spacer24dp)
            add(SignedOut.OnBoarding(walkThrough = mutableListOf(
                    Page1.toWalkThrough(),
                    Page2.toWalkThrough(),
                    Page3.toWalkThrough(),
                    Page4.toWalkThrough(),
                )))
            add(CommonItem.Spacer24dp)
            add(CommonItem.SectionDivider)
        }
    }
}



@Immutable
data class WalkThrough(@DrawableRes val resourceId: Int, @StringRes val stringId: Int,val  automationTestScreenLocator: String)
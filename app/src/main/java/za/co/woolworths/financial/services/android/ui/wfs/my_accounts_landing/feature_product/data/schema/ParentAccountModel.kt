package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema.MyOfferData
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema.OfferItemViewColor
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_onboarding.schema.WalkThrough
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountOfferKeys
import javax.annotation.concurrent.Immutable


sealed interface ParentAccountModel
sealed interface SignedOut : ParentAccountModel {
    data class OnBoarding(val walkThrough: List<WalkThrough>) : SignedOut
}

sealed interface CommonItem : ParentAccountModel {

    data class Toolbar(@StringRes val title: Int) : CommonItem
    data class Header(@StringRes val title: Int, val automationLocatorKey: String? = null) : CommonItem
    data class General(
        @DrawableRes val icon: Int? = null,
        @StringRes val title: Int? = null,
        val isShimmerDividedByTwo: Boolean = false, // required to display shorter shimmer width
        @DrawableRes val rightIcon: Int = R.drawable.ic_right,
        val automationLocatorKey: String = "",
        val clickable: OnAccountItemClickListener
    ) : CommonItem

    data class UserOffersAccount(val offers: MutableMap<AccountOfferKeys, OfferItem?>) :
        CommonItem

    data class OfferItem(
        val key : String,
        val data: MyOfferData,
        val properties: OfferItemViewColor,
        val automationLocatorKey: String,
        val onClick: OfferClickEvent
    ) : CommonItem

    data class UserAccountApplicationInfo(
        val appVersion: String?,
        @StringRes val fspNumberInfo: Int = R.string.fsp_number_info
    ) : CommonItem

    object Divider : CommonItem
    object SectionDivider : CommonItem
    object SpacerBottom : CommonItem
    object Spacer24dp : CommonItem
    object Spacer80dp : CommonItem
    object Spacer8dp : CommonItem
}

@Immutable
data class UserAccountInformation(
    @StringRes val greeting: Int = R.string.welcome_back,
    val username: String? = "",
    @DrawableRes val refreshIcon: Int = R.drawable.ic_refresh
)
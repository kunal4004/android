package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountOfferKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OfferClickEvent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel


@Composable
fun OfferCarousel(
    viewModel: UserAccountLandingViewModel,
    myOffers: MutableMap<AccountOfferKeys, CommonItem.OfferItem?>,
    isLoading: Boolean = false,
    isBottomSpacerShown : Boolean = false,
    brush: Brush? = null,
    onClick: (OfferClickEvent) -> Unit) {
    OfferViewMainList(viewModel = viewModel, myOffers, isLoading, isBottomSpacerShown = isBottomSpacerShown, brush) { onClick(it) }
}
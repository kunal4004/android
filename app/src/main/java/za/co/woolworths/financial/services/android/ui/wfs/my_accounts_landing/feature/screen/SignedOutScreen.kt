package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_container
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_toolbar_title
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.stabletype.GeneralProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.ui.GeneralItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui.OfferCarousel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_onboarding.ui.OnBoardingCarousel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.SignedOut
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_version_info.ui.ApplicationInfoView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter

@Composable
fun SignedOutScreen(
    viewModel: UserAccountLandingViewModel,
    onClick: (OnAccountItemClickListener) -> Unit) {
    val listOfSignedOutItems = remember {viewModel.listOfSignedOutItem()}
    ClearBadgeCountInLogoutState()
    Column(
        modifier = Modifier
            .testAutomationTag(locator = sign_out_container)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        listOfSignedOutItems.forEach { data ->
            when (data) {
                is CommonItem.Header -> HeaderItem(
                    title = stringResource(id = data.title),
                    locator = data.automationLocatorKey ?: "")

                is CommonItem.Toolbar -> {
                    val title = stringResource(id = data.title)
                    ToolbarView(title)
                }
                is CommonItem.UserAccountApplicationInfo -> ApplicationInfoView(applicationInfo = data)
                is CommonItem.UserOffersAccount -> OfferCarousel(viewModel = viewModel, data.offers){ onClick(it) }

                is GeneralProductType -> GeneralItem(data, isLoading = false) { onClick(it) }
                is SignedOut.OnBoarding -> OnBoardingCarousel(items = data.walkThrough) { onClick(it) }
                CommonItem.SectionDivider -> SectionDivider()
                CommonItem.Spacer80dp -> SpacerHeight80dp(bgColor = OneAppBackground)
                CommonItem.Spacer24dp -> SpacerHeight24dp()
                CommonItem.SpacerBottom -> SpacerHeight6dp(bgColor = OneAppBackground)
                CommonItem.Divider -> DividerThicknessOne()
                else -> Unit
            }
        }
    }
}

@Composable
private fun ClearBadgeCountInLogoutState() {
    LaunchedEffect(true) {
        QueryBadgeCounter.instance.clearBadge()
    }
}

@Composable
private fun ToolbarView(title: String, locator : String = sign_out_on_boarding_toolbar_title) {
    TextFuturaFamilyHeader1(
        modifier = Modifier
            .fillMaxWidth(),
        text = title,
        locator = locator,
        isUpperCased = true,
        textAlign = TextAlign.Center,
        fontSize = FontDimensions.sp12,
        textColor = Obsidian)
}
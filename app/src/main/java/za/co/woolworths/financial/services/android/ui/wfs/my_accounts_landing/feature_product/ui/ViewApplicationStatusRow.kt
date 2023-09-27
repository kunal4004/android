package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import za.co.woolworths.financial.services.android.ui.wfs.component.SurfaceTextButton
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.FunctionalGreys
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.Obsidian
import za.co.woolworths.financial.services.android.ui.wfs.theme.White


@Composable
fun ProductViewApplicationStatusView(applicationStatus: AccountProductCardsGroup.ApplicationStatus,
                                     onClick: (AccountProductCardsGroup) -> Unit) {

    if (!applicationStatus.isLoadingInProgress.isAccountLoading) {
        Card(
            modifier = Modifier
                .padding(start = Margin.start, end = Margin.end, bottom = Margin.dp3, top = Margin.dp16)
                .testAutomationTag(AutomationTestScreenLocator.my_products_section_row_view_application_status_card)
                .fillMaxWidth()
                .background(White)
                .bounceClick {
                    onClick(applicationStatus)
                }, shape = MaterialTheme.shapes.extraLarge) {

            Row(
                modifier = Modifier
                    .testAutomationTag(AutomationTestScreenLocator.my_products_section_row_view_application_status_card_row)
                    .background(FunctionalGreys)
                    .fillMaxSize()
                    .padding(start = Margin.start, end = Margin.end),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextOpenSansFontFamily(
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.W600,
                    color = Obsidian,
                    text = stringResource(id = applicationStatus.title),
                    letterSpacing = TextUnit.Unspecified,
                    fontSize = FontDimensions.sp13,
                    locator = AutomationTestScreenLocator.my_products_section_row_view_application_status_card_application_status_title_text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                SurfaceTextButton(
                    buttonLabel = stringResource(id = applicationStatus.buttonLabel),
                    isClickable = false,
                    locator = AutomationTestScreenLocator.my_products_section_row_view_application_status_card_application_status_view_button,
                    modifier = Modifier.padding(top = Margin.dp16, bottom = Margin.dp16),
                    bgColor = Obsidian
                ) { }
            }
        }
    }

    if (applicationStatus.isLoadingInProgress.isAccountLoading) {
        ProductShimmerView(key = applicationStatus.properties.automationLocatorKey, height = Dimens.fifty_dp)
    }

}
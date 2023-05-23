package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.awfs.coordination.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_application_status_card
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_link_woolies_card_shimmer_row
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_link_woolies_card_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_no_c2id_product_view_column
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema.OfferProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui.OfferShimmerView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui.OfferViewRow
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountOfferKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
@Composable
fun NoC2IdNorProductView(
    isLoadingInProgress: Boolean,
    isBottomSpacerShown : Boolean = false,
        brush: Brush?,
    onClick: (AccountProductCardsGroup.ApplicationStatus) -> Unit
) {
    brush ?: return

    Column(
        modifier = Modifier
            .testAutomationTag(my_products_section_box_no_c2id_product_view_column)
            .fillMaxWidth()
            .padding(top = Margin.dp16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoadingInProgress) {
            OfferShimmerView(brush = brush)
        }

        if (!isLoadingInProgress) {

            ViewApplicationStatusCard(
                isBottomSpacerShown = isBottomSpacerShown,
                isLoading = isLoadingInProgress,
                brush = brush,
                onClick = onClick
            )
        }

    }
}

@Composable
fun LinkYourWooliesCardUI(
    isLoadingInProgress: Boolean,
    brush: Brush?,
    onClick: (AccountProductCardsGroup.LinkYourWooliesCard) -> Unit
) {
    brush ?: return
    val labelLinkYourWooliesCard = stringResource(id = R.string.link_your_woolies_card).uppercase()
    if (isLoadingInProgress) {
        Row(
            modifier = Modifier
                .testAutomationTag(my_products_section_box_link_woolies_card_shimmer_row)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ShimmerLabelWithRoundedCorner(
                brush = brush,
                width = Shimmer.pointFiveFiveF,
                height = Shimmer.tenDp
            )
        }
    }

    if (!isLoadingInProgress) {
        TextOpenSansFontFamily(
            text = labelLinkYourWooliesCard,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Obsidian,
            fontSize = FontDimensions.sp14,
            locator = my_products_section_box_link_woolies_card_text,
            modifier = Modifier
                .padding(top = Margin.dp16)
                .fillMaxWidth()
                .bounceClick { onClick(AccountProductCardsGroup.LinkYourWooliesCard()) },
            textDecoration = TextDecoration.Underline
        )
    }
}


@Composable
fun ViewApplicationStatusCard(
    isBottomSpacerShown:Boolean = false,
    isLoading :Boolean = false,
    brush: Brush? = null,
    onClick: (AccountProductCardsGroup.ApplicationStatus) -> Unit){
    val lists: MutableMap<AccountOfferKeys, CommonItem.OfferItem?> = mutableMapOf(
        AccountOfferKeys.ViewApplicationStatus to OfferProductType.ViewApplicationStatus.value()
    )
    Column(modifier = Modifier.padding(start = Margin.start, end = Margin.end)) {
        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .testAutomationTag(my_application_status_card)
                .bounceClick { onClick(AccountProductCardsGroup.ApplicationStatus()) }
        ) {
            lists.values.toMutableList()[0]?.let { statusItem ->
                OfferViewRow(
                    item = statusItem, isLoading,
                    brush, listSize = 1
                )
            }
            }

        if (isBottomSpacerShown) {
            SpacerHeight24dp()
        }
    }
}
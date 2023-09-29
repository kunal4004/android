package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.wfs.component.MyIcon
import za.co.woolworths.financial.services.android.ui.wfs.component.ShimmerIconLabel
import za.co.woolworths.financial.services.android.ui.wfs.component.ShimmerLabel
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_general_row
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.general_arrow_icon
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.general_icon
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.general_row
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.general_title
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.setBadgeCounter
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.stabletype.GeneralProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.ui.UpdateMessageCount
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.Obsidian

@Preview
@Composable
fun GeneralItemPreview() {
    val row = GeneralProductType.Messages()
    GeneralItem(item= row){}
}

@Composable
fun GeneralItem(
    item: GeneralProductType?,
    isLoading: Boolean = false,
    onClickListener: (OnAccountItemClickListener) -> Unit
) {
    item ?: return

    val generalItem = GeneralProductType.getGeneralItems(item)
    val unreadMessageCount = if (item is GeneralProductType.Messages) item.unreadMessageCount else 0
    val locator = generalItem.automationLocatorKey

    when (item) {
        is GeneralProductType.Messages -> {
            val activity = LocalContext.current.findActivity()
            activity?.let {
                SideEffect {
                    setBadgeCounter(unreadMessageCount)
                    (it as? BottomNavigationActivity)?.addBadge(
                        BottomNavigationActivity.INDEX_ACCOUNT,
                        unreadMessageCount
                    )
                }
            }
        }
        else -> Unit
    }

    if (isLoading) {
        GeneralItemShimmer(generalItem)
    }

    if (!isLoading) {
        val title = generalItem.title?.let { stringResource(id = it) } ?: ""
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, color = Obsidian),
                ) { onClickListener(generalItem.clickable) }
                .testAutomationTag(locator = createLocator(default = general_row, key = locator) )
                .padding(top = Margin.start, bottom = Margin.end)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {

            MyIcon(
                id = generalItem.icon,
                modifier = Modifier
                    .testAutomationTag(locator = createLocator(default = general_icon, key = locator) )
                    .padding(start = Margin.start, end = Margin.dp16)
                    .size(Dimens.icon_size_dp)
            )

            TextOpenSansFontFamily(
                text = title,
                textAlign = TextAlign.Start,
                color = Color.Black,
                locator = createLocator(default = general_title, key = locator),
                letterSpacing = 0.sp,
                fontSize = FontDimensions.sp15,
                fontWeight = FontWeight.W400,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            if (unreadMessageCount > 0) {
                UpdateMessageCount(unreadMessageCount)
            } else {
                MyIcon(
                    id = generalItem.rightIcon,
                    contentDescriptionId = R.string.next_arrow,
                    modifier = Modifier
                        .testAutomationTag(locator = createLocator(default = general_arrow_icon, key = locator))
                        .padding(end = Margin.dp16)
                )
            }
        }
    }
}

@Composable
fun GeneralItemShimmer(row: CommonItem.General) {
    val locator = row.automationLocatorKey
    Row(
        modifier = Modifier
            .padding(
                top = Margin.top,
                bottom = Margin.bottom,
                end = Margin.end
            )
            .testAutomationTag(locator = createLocator(default = box_shimmer_general_row, key = locator))
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier
                .padding(start = Margin.start)
                .testAutomationTag(locator = createLocator(default = box_shimmer_general_row, key = locator)),
        verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerIconLabel()
            Spacer(modifier = Modifier.width(Margin.start))
            ShimmerLabel(
                width = if (row.isShimmerDividedByTwo) 0.25f else 0.5f,
                height = Dimens.ten_dp
            )
        }

        ShimmerIconLabel()

    }
}
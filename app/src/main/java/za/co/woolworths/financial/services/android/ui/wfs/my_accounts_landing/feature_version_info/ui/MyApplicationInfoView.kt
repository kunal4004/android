package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_version_info.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.component.ShimmerLabel
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight6dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.app_version_column
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.app_version_version_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.app_version_woolies_financial_services_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Gray
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground

@Composable
fun ApplicationInfoView(
    applicationInfo: CommonItem.UserAccountApplicationInfo,
    isLoading: Boolean = false
) {
    if (isLoading) {
        ApplicationInfoViewShimmer()
    }
    if (!isLoading) {
        Column(
            modifier = Modifier
                .testAutomationTag(app_version_column)
                .fillMaxWidth()
                .background(OneAppBackground)
        ) {
            Spacer(modifier = Modifier.height(Margin.dp16))
            TextOpenSansFontFamily(text = applicationInfo.appVersion ?: "",
                textAlign = TextAlign.Center,
                fontSize = FontDimensions.sp11,
                locator = app_version_version_text,
                letterSpacing = Dimens.zero_sp,
                modifier = Modifier
                    .fillMaxWidth(),
                color = Gray)
            SpacerHeight6dp(bgColor = Color.Transparent)
            TextOpenSansFontFamily(text = stringResource(id = applicationInfo.fspNumberInfo),
                textAlign = TextAlign.Center,
                fontSize = FontDimensions.sp11,
                locator = app_version_woolies_financial_services_text,
                modifier = Modifier
                    .fillMaxWidth(),
                color = Gray)
        }
    }

}

@Composable
fun ApplicationInfoViewShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OneAppBackground)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ShimmerLabel(width = 0.5f, height = 6.dp)
        }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ShimmerLabel(width = 0.8f, height = 6.dp)
            }
            Spacer(modifier = Modifier.height(17.dp))
        }
}
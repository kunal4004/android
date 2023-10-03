package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_welcome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import za.co.woolworths.financial.services.android.ui.wfs.component.MyIcon
import za.co.woolworths.financial.services.android.ui.wfs.component.ShimmerIconWithRoundedCorner
import za.co.woolworths.financial.services.android.ui.wfs.component.ShimmerLabelWithRoundedCorner
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFamilyBoldH1
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansMediumH3
import za.co.woolworths.financial.services.android.ui.wfs.component.rotationAnimation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_child_column
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_child_row
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_child_row_box
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_column
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_name_family_name_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_refresh_icon
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.welcome_section_welcome_back_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.conditional
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.noRippleClickable
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.RefreshAccountItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.Shimmer
import za.co.woolworths.financial.services.android.ui.wfs.theme.TextLightSilver

@Composable
fun WelcomeSectionView(
    viewModel: UserAccountLandingViewModel,
    icon: Int,
    isLoadingInProgress: Boolean,
    isRotating: Boolean,
    isRotatingState: (Boolean) -> Unit,
    onClick: (OnAccountItemClickListener) -> Unit
) {
    val rotation = rotationAnimation()
    val greetings = stringResource(id = viewModel.getUsernameAndGreeting().greeting)

    Column(
        Modifier
            .testAutomationTag(welcome_section_column)
            .background(Color.White)
            .padding(top = Margin.top)
    ) {

        Column(
            modifier = Modifier
                .testAutomationTag(welcome_section_child_column)
                .fillMaxWidth()
                .padding(start = Margin.start, end = Margin.end)
        ) {

            if (isLoadingInProgress) {
                ShimmerLabelWithRoundedCorner(
                    width = Shimmer.point35F,
                    height = Shimmer.sevenDp
                )
            }

            if (!isLoadingInProgress) {
                TextOpenSansMediumH3(
                    text = greetings,
                    fontSize = FontDimensions.sp12,
                    locator = welcome_section_welcome_back_text,
                    color = TextLightSilver,
                    isUpperCased = true
                )
            }

            NameAndRefreshButtonView(
                viewModel,
                isLoadingInProgress,
                isRotating,
                rotation,
                isRotatingState,
                onClick,
                icon
            )

        }

        SpacerHeight24dp()

    }
}

@Composable
private fun NameAndRefreshButtonView(
    viewModel: UserAccountLandingViewModel,
    isLoadingInProgress: Boolean,
    isRotating: Boolean,
    rotation: Float, isRotatingState: (Boolean) -> Unit,
    onClick: (OnAccountItemClickListener) -> Unit,
    icon: Int
) {
    Row(
        modifier = Modifier
            .testAutomationTag(welcome_section_child_row)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {

        if (isLoadingInProgress) {
            Box(
                modifier = Modifier
                    .testAutomationTag(welcome_section_child_row_box)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ShimmerLabelWithRoundedCorner(
                    width = Shimmer.pointFiveFiveF,
                    height = Shimmer.tenDp
                )

            }
        }

        if (!isLoadingInProgress) {
            TextOpenSansFamilyBoldH1(
                text = viewModel.getNameAndFamilyName(),
                color = Color.Black,
                fontSize = FontDimensions.sp20,
                locator = welcome_section_name_family_name_text,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        if (!isLoadingInProgress && viewModel.isC2UserOrMyProductItemExist()) {
            Box(modifier = Modifier
                .conditional(isRotating, ifTrue = { rotate(rotation) }, ifFalse = null)
                .noRippleClickable {
                    isRotatingState(true)
                    onClick(RefreshAccountItem)
                }
            ) { MyIcon(id = icon, modifier =  Modifier.testAutomationTag(welcome_section_refresh_icon)) }
        }

        if (isLoadingInProgress) {
            ShimmerIconWithRoundedCorner()
        }
    }
}
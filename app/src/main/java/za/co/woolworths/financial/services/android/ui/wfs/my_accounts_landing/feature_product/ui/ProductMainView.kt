package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.component.TextWFuturaMedium
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_row_good_standing_retry_load_desc_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductProperties
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductTransformer
import za.co.woolworths.financial.services.android.ui.wfs.theme.BrightGray
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.LetterSpacing
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

private const val columnRef = "columnRef"
private const val backgroundImage = "backgroundImage"

@Composable
fun ColumnScope.ProductInGoodStandingAndInArrearsContainer(
    transformer: ProductTransformer,
    titleLocator: String,
    title: String,
    isProductInGoodStandingAndRetryButtonDisabled: Boolean,
    locator: String,
    availableText: String,
    isRetryButtonEnabled: Boolean,
    isRetryInProgress: Boolean,
    isProductInGoodStandingAndRetryButtonEnabled: Boolean,
    accountInArrearsLocator: String,
    properties: ProductProperties) {
    MyProductTitleText(titleLocator = titleLocator, title = title.uppercase())
    SpacerHeight8dp(bgColor = Color.Transparent)
    AnimatedVisibility(visible = isProductInGoodStandingAndRetryButtonDisabled) {
        MyProductAvailableBalanceRow(
            locator = locator,
            descriptionLabel = availableText,
            descriptionValue = transformer.currentAmount
        )
    }

    RetryButtonEnabled(
        isRetryButtonEnabled = isRetryButtonEnabled,
        isRetryInProgress = isRetryInProgress,
        locator = locator
    )

    EnableInArrearsTextForProductNotInGoodStanding(
        isProductInGoodStandingAndRetryButtonEnabled = isProductInGoodStandingAndRetryButtonEnabled,
        accountInArrearsLocator = accountInArrearsLocator,
        properties = properties
    )
}

@Composable
private fun ColumnScope.EnableInArrearsTextForProductNotInGoodStanding(
    isProductInGoodStandingAndRetryButtonEnabled: Boolean,
    accountInArrearsLocator: String,
    properties: ProductProperties) {
    AnimatedVisibility(visible = isProductInGoodStandingAndRetryButtonEnabled) {
        val accountInArrearsText = stringResource(id = properties.accountInArrearsLabel).uppercase()
        TextWFuturaMedium(
            text = accountInArrearsText,
            locator = accountInArrearsLocator,
            fontFamily = FuturaFontFamily,
            fontSize = FontDimensions.sp13,
            letterSpacing = LetterSpacing.ls12,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun RetryButtonEnabled(
    isRetryButtonEnabled: Boolean,
    isRetryInProgress: Boolean,
    locator : String?) {
    if (isRetryButtonEnabled) {
        val retryInProgress = createLocator(default = my_products_section_box_constraint_row_good_standing_retry_load_desc_text, key = locator)
        val loadingText =
            if (isRetryInProgress) R.string.my_products_loading else R.string.my_products_cant_load_product
        TextOpenSansFontFamily(
            locator = retryInProgress,
            text = stringResource(loadingText),
            color = BrightGray,
            fontWeight = FontWeight.W400,
            letterSpacing = Dimens.zero_sp,
            fontSize = FontDimensions.sp13
        )
    }
}

@Composable
fun AccountInArrearsOrChargedOffBadge(
    isAccountInArrears: Boolean,
    accountInArrearsLocator: String,
    properties: ProductProperties
) {
    AnimatedVisibility(visible = isAccountInArrears) {
        val density = LocalDensity.current
        val offsetInPx by remember { mutableStateOf(density.run { (16.dp / 2).roundToPx() })}
        Image(
            modifier = Modifier
                .testAutomationTag(accountInArrearsLocator)
                .offset { IntOffset(x = +offsetInPx, y = -offsetInPx) },
            painter = painterResource(id = properties.accountInArrearsIcon),
            contentDescription = accountInArrearsLocator
        )
    }
}

@Composable
private fun MyProductAvailableBalanceRow(
    locator: String,
    descriptionLabel: String,
    descriptionValue: String?) {

    val availableFundTitleLocator = createLocator(default = AutomationTestScreenLocator.my_products_section_box_constraint_row_good_standing_desc_key_title_text, key =locator )
    val availableFundValueLocator = createLocator(default = AutomationTestScreenLocator.my_products_section_box_constraint_row_good_standing_desc_value_title_text, key =locator )

    Row (modifier = Modifier.fillMaxWidth()){
        TextOpenSansFontFamily(
            locator = availableFundTitleLocator,
            color = BrightGray,
            text = descriptionLabel, fontSize = FontDimensions.sp15)

        Spacer(modifier = Modifier.width(Dimens.four_dp))

        TextOpenSansFontFamily(
            locator = availableFundValueLocator,
            text =  descriptionValue ?: "",
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = FontDimensions.sp15,
            color = White
        )
    }
}

fun createConstraints(): ConstraintSet {
    return ConstraintSet {
        val imageRef = createRefFor(backgroundImage)
        val columnRef = createRefFor(columnRef)
        // Constraint the background image to the parent edges
        constrain(imageRef) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        // Constraint the column to the edges of the background image and set its height to match constraints
        constrain(columnRef) {
            top.linkTo(imageRef.top)
            bottom.linkTo(imageRef.bottom)
            start.linkTo(imageRef.start)
            end.linkTo(imageRef.end)
            height = Dimension.fillToConstraints
        }
    }
}
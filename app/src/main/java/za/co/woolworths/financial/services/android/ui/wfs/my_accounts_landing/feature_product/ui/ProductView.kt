package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_row_good_standing_title_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_row_in_arrears_title_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.ui.columnRef
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductProperties
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductTransformer
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.RetryOptions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin

@Composable
fun ProductContainerSwitcher(productGroup: AccountProductCardsGroup,
                             onProductClick: (AccountProductCardsGroup) -> Unit) { //onClick: (OnAccountItemClickListener) -> Unit

    when (productGroup) {

        is AccountProductCardsGroup.BlackCreditCard -> ProductViewItem(
            retryOptions = productGroup.retryOptions,
            transformer = productGroup.transformer,
            properties = productGroup.properties,
            onProductClick = {
                val retryOptions = productGroup.retryOptions
                val isRetryButtonEnabled = retryOptions.isRetryButtonEnabled
                val productGroups = productGroup.copy(
                    retryOptions = retryOptions.copy(
                        isRetryInProgress = isRetryButtonEnabled
                    )
                )
                onProductClick(productGroups)
            }
        )

        is AccountProductCardsGroup.GoldCreditCard -> ProductViewItem(
            retryOptions = productGroup.retryOptions,
            transformer = productGroup.transformer,
            properties = productGroup.properties,
            onProductClick = {
                val retryOptions = productGroup.retryOptions
                val isRetryButtonEnabled = retryOptions.isRetryButtonEnabled
                val productGroups = productGroup.copy(
                    retryOptions = retryOptions.copy(
                        isRetryInProgress = isRetryButtonEnabled
                    )
                )
                onProductClick(productGroups)
            }
        )

        is AccountProductCardsGroup.SilverCreditCard -> ProductViewItem(
            retryOptions = productGroup.retryOptions,
            transformer = productGroup.transformer,
            properties = productGroup.properties,
            onProductClick = {
                val retryOptions = productGroup.retryOptions
                val isRetryButtonEnabled = retryOptions.isRetryButtonEnabled
                val productGroups = productGroup.copy(
                    retryOptions = retryOptions.copy(
                        isRetryInProgress = isRetryButtonEnabled
                    )
                )
                onProductClick(productGroups)
            }
        )

        is AccountProductCardsGroup.PersonalLoan -> ProductViewItem(
            retryOptions = productGroup.retryOptions,
            transformer = productGroup.transformer,
            properties = productGroup.properties,
            onProductClick = {
                val retryOptions = productGroup.retryOptions
                val isRetryButtonEnabled = retryOptions.isRetryButtonEnabled
                val productGroups = productGroup.copy(
                    retryOptions = retryOptions.copy(
                        isRetryInProgress = isRetryButtonEnabled
                    )
                )
                onProductClick(productGroups)
            }
        )

        is AccountProductCardsGroup.StoreCard -> ProductViewItem(
            retryOptions = productGroup.retryOptions,
            transformer = productGroup.transformer,
            properties = productGroup.properties,
            onProductClick = {
                val retryOptions = productGroup.retryOptions
                val isRetryButtonEnabled = retryOptions.isRetryButtonEnabled
                val productGroups = productGroup.copy(
                    retryOptions = retryOptions.copy(
                        isRetryInProgress = isRetryButtonEnabled
                    )
                )
                onProductClick(productGroups)
            }
        )

        else -> Unit

    }
}

@Composable
fun ProductViewItem(
    retryOptions: RetryOptions,
    transformer: ProductTransformer,
    properties: ProductProperties,
    onProductClick: () -> Unit ) {

    val locator = properties.automationLocatorKey
    val isAccountInArrears = transformer.isAccountInArrears || transformer.isAccountChargedOff
    val isRetryButtonEnabled = retryOptions.isRetryButtonEnabled
    val isProductInGoodStandingAndRetryButtonDisabled = !isAccountInArrears && !isRetryButtonEnabled
    val isProductInGoodStandingAndRetryButtonEnabled = isAccountInArrears && !isRetryButtonEnabled

    var buttonState by remember { mutableStateOf(ButtonState.IDLE) }

    buttonState = when (retryOptions.isRetryInProgress) {
        true -> ButtonState.LOADING
        false -> ButtonState.IDLE
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .testAutomationTag(createLocator(my_products_section_box, locator))
            .padding(start = Margin.start, end = Margin.end, top = Margin.dp16)
            .bounceClick { onProductClick.invoke() },
        contentAlignment = Alignment.TopEnd
    ) {
        ConstraintLayout(constraintSet = createConstraints()) {
            BackgroundImage(
                properties = properties,
                title = stringResource(id = properties.productTitle),
                locator = locator
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .layoutId(columnRef)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .fillMaxHeight()
                        .padding(start = Margin.start),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    ProductInGoodStandingAndInArrearsContainer(
                        titleLocator = createLocator(
                            default = my_products_section_box_constraint_row_good_standing_title_text,
                            key = locator
                        ),
                        title = stringResource(id = properties.productTitle),
                        isProductInGoodStandingAndRetryButtonDisabled = isProductInGoodStandingAndRetryButtonDisabled,
                        locator = locator,
                        availableText = stringResource(id = properties.availableProduct),
                        isRetryButtonEnabled = retryOptions.isRetryButtonEnabled,
                        isRetryInProgress = retryOptions.isRetryInProgress,
                        transformer = transformer,
                        isProductInGoodStandingAndRetryButtonEnabled =isProductInGoodStandingAndRetryButtonEnabled,
                        accountInArrearsLocator = createLocator(
                            default = my_products_section_box_constraint_row_in_arrears_title_text,
                            key = locator
                        ),
                        properties = properties
                    )
                }

                ViewRetryMyCoverButtonGroup(
                    locator = locator,
                    buttonType = if (isRetryButtonEnabled) MyProductButtonType.RETRY else MyProductButtonType.VIEW,
                    buttonState = buttonState,
                    viewButtonLabel = stringResource(id = properties.viewButton).uppercase(),
                    retryButtonLabel = stringResource(id = properties.retryButton).uppercase()
                )
            }
        }

        AccountInArrearsOrChargedOffBadge(
            isAccountInArrears = isAccountInArrears,
            accountInArrearsLocator = createLocator(
                default = my_products_section_box_constraint_row_in_arrears_title_text,
                key = locator
            ),
            properties = properties
        )

    }
}
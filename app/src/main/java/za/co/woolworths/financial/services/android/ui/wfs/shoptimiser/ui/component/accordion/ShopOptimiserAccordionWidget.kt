package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ShopOptimiserVisibleUiType
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.enterExpandVerticallyFadeInAnimation
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.exitShrinkVerticallyFadeOutAnimation
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.standalone.ShopOptimiserPayFlexStandAloneUI
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
/**
 * Composable function for rendering the Shop Optimiser Accordion widget.
 * Determines the visibility and content of the widget based on the shopOptimiserVisibleType.
 */
@Composable
fun ShopOptimiserViewModel.ShopOptimiserAccordionWidget() {

    // Collect the shopOptimiserVisibleType state
    val shopOptimiserVisibility by shopOptimiserVisibleType.collectAsState()

    // Choose the content to display based on shopOptimiserVisibility
    when(shopOptimiserVisibility) {
        ShopOptimiserVisibleUiType.ACCORDION -> { ShopOptimiserAccordionUI() }
        ShopOptimiserVisibleUiType.STANDALONE -> { ShopOptimiserPayFlexStandAloneUI() }
        ShopOptimiserVisibleUiType.GONE -> Unit
    }

    // Initialize the ShopOptimiserAccordionController
    ShopOptimiserAccordionController()
}

/**
 * Composable function for rendering the Shop Optimiser Accordion user interface.
 * Displays a list of products with expandable content.
 */
@Composable
private fun ShopOptimiserViewModel.ShopOptimiserAccordionUI() {
    val enterTransition: EnterTransition = enterExpandVerticallyFadeInAnimation()
    val exitTransition: ExitTransition = exitShrinkVerticallyFadeOutAnimation()

    Column {
        // Render the parent item for the accordion
        ShopOptimiserAccordionParentItem(viewModel = this@ShopOptimiserAccordionUI)

        // Animate visibility of the accordion content
        AnimatedVisibility(
            visible = isExpanded,
            enter = enterTransition,
            exit = exitTransition) {
            Column {
                // Iterate through the list of products and display each item
                for (productOnDisplay in shoptimiserProductsList) {
                    ShopOptimiserAccordionContent(productOnDisplay)
                }
            }
        }
    }
}

/**
 * Composable function responsible for controlling the Shop Optimiser Accordion widget.
 * Manages the visibility and behavior of the widget based on user accounts and states.
 */
@Composable
fun ShopOptimiserViewModel.ShopOptimiserAccordionController() {

    // Collect the user account response from the flow
    val accountResponse by userAccountsFlow.collectAsStateWithLifecycle()

    // Check if the product detail page was reopened and set the accordion UI visible if necessary
    if (productDetailPageWasReopened()) {
        setAccordionUIVisible()
    }

    // Check if Shop Optimiser is enabled
    if (isShopOptimiserEnabled()) {
        setAccordionUIVisible()

        // If the account response is still loading, create the Shop Optimiser product
        if (accountResponse.isLoading) {
            createShopOptimiserProduct()
        }

        // Process the user account response
        accountResponse.data?.let { accountsData ->
            setUserAccountResponse(userAccountResponse = accountsData) { isStandAloneViewVisible ->
                when (isStandAloneViewVisible) {
                    true -> setStandaloneUIVisible()
                    false -> setAccordionUIVisible()
                }
            }
        }

        // Handle the case when the account response has an error
        if (accountResponse.hasError) {
            setStandaloneUIVisible()
            removeLoaderWhenAccountHasError()
        }
    } else {
        // If Shop Optimiser is not enabled, set the standalone UI visible
        setStandaloneUIVisible()
    }
}
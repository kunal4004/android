package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ShopOptimiserVisibleUiType
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.standalone.ShopOptimiserPayFlexStandAloneUI
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
/**
 * Composable function for rendering the Shop Optimiser Accordion widget.
 * Determines the visibility and content of the widget based on the shopOptimiserVisibleType.
 */
@Composable
fun ShopOptimiserViewModel.ShopOptimiserAccordionWidget() {

    // Choose the content to display based on shopOptimiserVisibleUiType
    when(shopOptimiserVisibleUiType) {
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


    val enterTransition: EnterTransition  = remember {  expandVertically(
        expandFrom = Alignment.Top,
        animationSpec = tween(ShopOptimiserConstant.EXPAND_TRANSITION_DURATION)
    ) + fadeIn(
        initialAlpha = 0.3f,
        animationSpec = tween(ShopOptimiserConstant.EXPAND_TRANSITION_DURATION)
    )
    }

    val exitTransition: ExitTransition =  remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(ShopOptimiserConstant.EXPAND_TRANSITION_DURATION)
        ) + fadeOut(
            animationSpec = tween(ShopOptimiserConstant.EXPAND_TRANSITION_DURATION)
        )
    }

    val listOfProductsOnDisplay = remember { shoptimiserProductsList }

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
                for (productOnDisplay in listOfProductsOnDisplay) {
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
    val accountResponse by userAccountsFlow.collectAsState(initial = NetworkStatusUI())

    // Check if the product detail page was reopened and set the accordion UI visible if necessary
    if (wasProductDetailPageReOpened()) {
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
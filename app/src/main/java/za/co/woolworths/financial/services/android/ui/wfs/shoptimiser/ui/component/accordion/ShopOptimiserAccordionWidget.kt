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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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

    val enterTransition: EnterTransition  = remember { expandVertically(
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
    val accountResponse by userAccountsFlow.collectAsState(initial = NetworkStatusUI())

    // Using derivedStateOf to minimize unnecessary calculations
    var isAccordionUIVisible = remember(accountResponse) {
        (accountResponse.isLoading || isShopOptimiserEnabled()) && !isPayFlexViewVisible
    }

    // Handle UI updates based on the derived state and events
    LaunchedEffect(accountResponse) {
        if (isAccordionUIVisible) {
            setAccordionUIVisible()
        } else {
            setStandaloneUIVisible()
        }

        if (accountResponse.isLoading) {
            createShopOptimiserProduct()
        }

        accountResponse.data?.let { accountsData ->
            setUserAccountResponse(userAccountResponse = accountsData) { isStandAloneViewVisible ->
                isAccordionUIVisible = !isStandAloneViewVisible
                isPayFlexViewVisible = isStandAloneViewVisible
                if (isStandAloneViewVisible) {
                    setStandaloneUIVisible()
                } else {
                    setAccordionUIVisible()
                }
            }
        }

        if (accountResponse.hasError) {
            removeLoaderWhenAccountHasError()
        }
    }

    // Another LaunchedEffect for handling product detail page reopening
    LaunchedEffect(key1 = wasProductDetailPageReOpened()) {
        if (wasProductDetailPageReOpened()) {
            if (isAccordionUIVisible)
                setAccordionUIVisible()
        }
    }

}
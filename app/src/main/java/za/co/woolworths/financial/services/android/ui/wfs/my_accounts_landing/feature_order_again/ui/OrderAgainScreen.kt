package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.awfs.coordination.R
import kotlinx.coroutines.flow.collectLatest
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.BlackRoundedCornerIcon
import za.co.woolworths.financial.services.android.presentation.common.BlackRoundedCornerText
import za.co.woolworths.financial.services.android.presentation.common.CircleIcon
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH1
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH14
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH15
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewEvent
import za.co.woolworths.financial.services.android.presentation.common.OpenSansText12
import za.co.woolworths.financial.services.android.presentation.common.OpenSansText14
import za.co.woolworths.financial.services.android.presentation.common.OpenSansTitleText10
import za.co.woolworths.financial.services.android.presentation.common.OpenSansTitleText13
import za.co.woolworths.financial.services.android.presentation.common.PromotionalText
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationEvent
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationViewState
import za.co.woolworths.financial.services.android.shoppinglist.view.SwipeListActionItem
import za.co.woolworths.financial.services.android.shoppinglist.view.SwipeToRevealView
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight12dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight40dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth8dp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.roundToPx
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainUiState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.SnackbarDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorLabel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.ShimmerColor
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun OrderAgainScreen(
    viewModel: OrderAgainViewModel,
    onBackPressed: () -> Unit,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    val state by viewModel.orderAgainUiState.collectAsStateWithLifecycle()
    val errorSnackBarState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.onScreenEvent.collectLatest { onScreenEvent ->
            when (onScreenEvent) {
                is OrderAgainScreenEvents.ShowErrorSnackBar -> {
                    errorSnackBarState.showSnackbar("")
                }

                else -> onEvent(onScreenEvent)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                SpacerHeight16dp()
                HeaderView(
                    modifier = Modifier.background(White),
                    headerViewState = state.headerState
                ) { event ->
                    when (event) {
                        HeaderViewEvent.IconClick -> onBackPressed()
                        HeaderViewEvent.RightButtonClick -> viewModel.onEvent(
                            OrderAgainScreenEvents
                                .SelectAllClick
                        )
                    }
                }
                SpacerHeight16dp()
                Divider(color = ColorD8D8D8)
            }
        }
    ) {

        OrderAgainStatelessScreen(
            modifier = Modifier.padding(it),
            state = state,
            orderList = viewModel.orderList,
            errorSnackBarState = errorSnackBarState
        ) { event ->
            when (event) {
                OrderAgainScreenEvents.ChangeDeliveryClick,
                OrderAgainScreenEvents.StartShoppingClicked,
                OrderAgainScreenEvents.CopyToListClicked,
                OrderAgainScreenEvents.ChangeAddressClick,
                is OrderAgainScreenEvents.CopyItemToListClicked -> onEvent(event)

                else -> viewModel.onEvent(event)
            }
        }
    }
}

@Composable
private fun OrderAgainStatelessScreen(
    modifier: Modifier = Modifier,
    state: OrderAgainUiState,
    orderList: List<ProductItem>,
    errorSnackBarState: SnackbarHostState,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    Column(modifier.background(OneAppBackground)) {

        ErrorSnackbarView(errorSnackBarState, state.snackbarData)

        DeliveryLocationView(state.deliveryState) {
            when (it) {
                DeliveryLocationEvent.ChangeAddressClick -> onEvent(OrderAgainScreenEvents.ChangeAddressClick)
                else -> onEvent(OrderAgainScreenEvents.ChangeDeliveryClick)
            }
        }
        SpacerHeight8dp(bgColor = OneAppBackground)
        when (state.screenState) {
            OrderAgainScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Black
                    )
                }
            }

            is OrderAgainScreenState.ShowErrorScreen -> EmptyScreen(
                Modifier.background(White),
                title = stringResource(id = state.screenState.title),
                subTitle = stringResource(id = state.screenState.subTitle)
            ) {
                onEvent(OrderAgainScreenEvents.StartShoppingClicked)
            }

            is OrderAgainScreenState.ShowEmptyScreen -> EmptyScreen(
                Modifier.background(White),
                title = stringResource(id = state.screenState.title),
                subTitle = stringResource(id = state.screenState.subTitle)
            ) {
                onEvent(OrderAgainScreenEvents.StartShoppingClicked)
            }

            OrderAgainScreenState.ShowOrderList -> {
                OrderAgainList(
                    Modifier.weight(1f),
                    orderList,
                    state.revealedList,
                    onEvent
                )
            }

            else -> {}
        }
        if (state.showAddToCart) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color.Black
                        )
                    }
                    BlackButton(
                        Modifier.fillMaxWidth(),
                        text = pluralStringResource(
                            id = R.plurals.plural_add_to_cart,
                            state.itemsToBeAddedCount,
                            state.itemsToBeAddedCount
                        ),
                        !state.isLoading
                    ) {
                        onEvent(OrderAgainScreenEvents.AddToCartClicked)
                    }
                }
                UnderlineButton(
                    Modifier.fillMaxWidth(),
                    text = stringResource(id = state.resIdCopyToList),
                    texColor = Black,
                    enabled = !state.isLoading
                ) {
                    onEvent(OrderAgainScreenEvents.CopyToListClicked)
                }
            }
        }
    }
}

@Composable
fun ErrorSnackbarView(errorSnackBarState: SnackbarHostState, snackbarData: SnackbarDetails) {
    SnackbarHost(
        hostState = errorSnackBarState,
    ) { snackBarData ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ErrorBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.info_white),
                tint = White,
                contentDescription = stringResource(R.string.cd_information_icon)
            )
            SpacerWidth8dp()
            OpenSansText12(
                text = stringResource(id = snackbarData.errorTitle),
                color = White
            )
        }
    }
}

@Composable
fun DeliveryLocationView(
    deliveryState: DeliveryLocationViewState,
    onEvent: (DeliveryLocationEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .background(White)
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OpenSansTitleText13(text = stringResource(id = deliveryState.resDeliveryType))
            BlackRoundedCornerIcon(
                icon = R.drawable.refresh_account_icon_normal,
                tintColor = White
            ) {
                onEvent(DeliveryLocationEvent.ChangeDeliveryClick)
            }
        }

        SpacerHeight16dp()
        Divider(color = ColorD8D8D8)
        SpacerHeight16dp()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OpenSansTitleText13(
                text = deliveryState.textDeliveryLocation.ifEmpty {
                    stringResource(
                        id = deliveryState.textDeliveryLocationRes
                    )
                }
            )
            BlackRoundedCornerIcon(
                icon = R.drawable.ic_edit_black, tintColor = White
            ) {
                onEvent(DeliveryLocationEvent.ChangeAddressClick)
            }
        }
    }
}

@Composable
fun OrderAgainList(
    modifier: Modifier = Modifier,
    orderList: List<ProductItem>,
    revealedList: List<String>,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    val state = rememberLazyListState()

    LazyColumn(modifier = modifier, state = state) {
        items(orderList) {

            SwipeToRevealView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                rowOffsetInPx = 116.dp.roundToPx(),
                animationDurationInMillis = 600,
                isRevealed = revealedList.contains(it.id),
                onExpand = {
                    onEvent(OrderAgainScreenEvents.ListItemRevealed(it))
                },
                onCollapse = {
                    onEvent(OrderAgainScreenEvents.ListItemCollapsed(it))
                },
                rowContent = {
                    ProductItemView(it, onEvent = onEvent)
                },
                actionContent = {
                    SwipeListActionItem(
                        modifier = Modifier
                            .width(116.dp)
                            .fillMaxHeight()
                            .background(Black),
                        icon = R.drawable.cart_icon,
                        textStyle = TextStyle(
                            color = White
                        ),
                        actionText = R.string.add_to_cart,
                        showLoading = it.inProgress,
                        tintColor = White,
                        progressBarColor = White
                    ) {
                        if (!it.inProgress)
                            onEvent(OrderAgainScreenEvents.OnSwipeAddAction(it))
                    }
                }
            )
            Divider(color = colorResource(id = R.color.color_D8D8D8))
        }
    }
}

@Composable
fun ProductItemView(
    productItem: ProductItem,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(visible = productItem.quantityInStock > 0) {
                Image(
                    modifier = Modifier.clickable {
                        onEvent(
                            OrderAgainScreenEvents.ProductItemCheckedChange(
                                !productItem.isSelected,
                                productItem
                            )
                        )
                    },
                    painter = painterResource(
                        id = if (productItem.isSelected)
                            R.drawable.check_mark_icon
                        else
                            R.drawable.uncheck_item
                    ),
                    contentDescription = stringResource(id = R.string.cd_product_checkbox)
                )
            }

            if (productItem.quantityInStock <= 0) {
                SpacerWidth24dp()
            }

            SpacerWidth16dp()

            AsyncImage(
                modifier = Modifier.size(80.dp, 112.dp),
                model = productItem.productImage,
                contentDescription = stringResource(id = R.string.cd_product_image),
                error = painterResource(id = R.drawable.image_placeholder)
            )

            SpacerWidth16dp()

            ProductItemDetails(modifier = Modifier.fillMaxHeight(), productItem, onEvent = onEvent)
        }

        if (productItem.promotionalText.isNotEmpty()) {
            SpacerHeight24dp()
            PromotionalText(
                text = productItem.promotionalText,
                textDecoration = TextDecoration.Underline,
                contentDesc = productItem.promotionalText
            )
        }
    }
}

@Composable
fun ProductItemDetails(
    modifier: Modifier = Modifier,
    productItem: ProductItem,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OpenSansText14(
                modifier = Modifier.weight(1f),
                text = productItem.productName,
                maxLines = 1
            )
            SpacerWidth8dp()
            Icon(
                modifier = Modifier
                    .alpha(if (productItem.isSelected) 0.5f else 1f)
                    .clickable(enabled = !productItem.isSelected) {
                        onEvent(OrderAgainScreenEvents.CopyItemToListClicked(productItem))
                    },
                painter = painterResource(id = R.drawable.ic_option_menu),
                contentDescription = stringResource(id = R.string.cd_options_menu)
            )
        }

        if (productItem.productAvailabilityResource != R.string.empty) {
            SpacerHeight12dp()
            BlackRoundedCornerText(
                text = stringResource(id = productItem.productAvailabilityResource).uppercase()
            )
            Spacer(modifier = Modifier.weight(1f, true))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {

            Column {
                if (productItem.wasPrice > 0.0) {
                    OpenSansTitleText10(
                        text = productItem.wasPriceString,
                        color = ErrorLabel,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                FuturaTextH14(text = productItem.priceString)
            }

            if (productItem.isSelected)
                QuantitySelectionView(
                    modifier = Modifier.weight(1f, false),
                    productItem = productItem,
                    leftIcon = if (productItem.quantity == 1) R.drawable.delete_24
                    else R.drawable.ic_minus_black,
                    leftIconEnabled = productItem.quantity > 1,
                    cdLeftIcon = stringResource(id = R.string.cd_product_decrease_quantity),
                    rightIconEnabled = productItem.quantity < productItem.quantityInStock,
                    cdRightIcon = stringResource(id = R.string.cd_product_increase_quantity),
                    onLeftIconClick = {
                        onEvent(OrderAgainScreenEvents.ChangeProductQuantityBy(-1, productItem))
                    },
                    onRightIconClick = {
                        onEvent(OrderAgainScreenEvents.ChangeProductQuantityBy(1, productItem))
                    }
                )
        }

    }
}

@Composable
fun QuantitySelectionView(
    modifier: Modifier = Modifier,
    leftIcon: Int = R.drawable.ic_minus_black,
    leftIconEnabled: Boolean = false,
    cdLeftIcon: String = "",
    rightIcon: Int = R.drawable.add_black,
    rightIconEnabled: Boolean = false,
    cdRightIcon: String = "",
    productItem: ProductItem,
    onLeftIconClick: () -> Unit,
    onRightIconClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIcon(leftIcon, leftIconEnabled, cdLeftIcon, background = ShimmerColor) {
            onLeftIconClick()
        }
        SpacerWidth8dp()
        FuturaTextH15(
            modifier = Modifier.widthIn(min = 24.dp),
            text = productItem.quantity.toString()
        )
        SpacerWidth8dp()
        CircleIcon(rightIcon, rightIconEnabled, cdRightIcon, ShimmerColor) {
            onRightIconClick()
        }
    }
}

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.image_placeholder,
    title: String = "",
    subTitle: String = "",
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(120.dp),
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.cd_empty_state_icon)
        )

        SpacerHeight24dp()
        FuturaTextH1(
            modifier = Modifier.padding(horizontal = 54.dp),
            text = title,
            textAlign = TextAlign.Center
        )
        SpacerHeight8dp()
        OpenSansText14(
            modifier = Modifier.padding(horizontal = 54.dp),
            text = subTitle,
            color = Color444444,
            textAlign = TextAlign.Center
        )
        SpacerHeight40dp()
        BlackButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = stringResource(id = R.string.start_shopping).uppercase()
        ) {
            onClick()
        }
    }

}


@Preview
@Composable
private fun PreviewOrderAgainScreen() {
    OneAppTheme {
        OrderAgainStatelessScreen(
            state = OrderAgainUiState(),
            orderList = emptyList(),
            errorSnackBarState = SnackbarHostState()
        ) {}
    }
}

@Preview
@Composable
private fun PreviewEmptyScreen() {
    OneAppTheme {
        EmptyScreen(Modifier.background(White)) {}
    }
}

@Preview
@Composable
private fun PreviewDeliveryLocation() {
    OneAppTheme {
        DeliveryLocationView(
            DeliveryLocationViewState(
                R.string.standard_delivery,
                "10 Krynauw Avenue, Gardens"
            )
        ) {}
    }
}

@Preview
@Composable
private fun PreviewProductItemView() {
    OneAppTheme {
        ProductItemView(
            ProductItem(
                productName = "Crumbed Trout Fish Cakes 600",
                promotionalText = "offer: BUY ANY 2 SAVE 20% fresh fruit",
                priceString = "R 99.86"
            ).apply {
                quantityInStock = 1
                isSelected = true
                productAvailabilityResource = R.string.empty
            }
        ) {}
    }
}

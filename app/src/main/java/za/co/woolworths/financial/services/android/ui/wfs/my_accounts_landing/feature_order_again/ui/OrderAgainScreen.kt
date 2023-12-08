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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.BlackRoundedCornerIcon
import za.co.woolworths.financial.services.android.presentation.common.BlackRoundedCornerText
import za.co.woolworths.financial.services.android.presentation.common.CircleIcon
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH1
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH10
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH12
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH14
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH15
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH8
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewEvent
import za.co.woolworths.financial.services.android.presentation.common.OpenSansText14
import za.co.woolworths.financial.services.android.presentation.common.OpenSansTitleText10
import za.co.woolworths.financial.services.android.presentation.common.OpenSansTitleText13
import za.co.woolworths.financial.services.android.presentation.common.PromotionalText
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationViewState
import za.co.woolworths.financial.services.android.shoppinglist.view.SwipeListActionItem
import za.co.woolworths.financial.services.android.shoppinglist.view.SwipeToRevealView
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight12dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight40dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight6dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidthDp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.roundToPx
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainUiState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorLabel
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.ShimmerColor
import za.co.woolworths.financial.services.android.ui.wfs.theme.SnackbarBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.White
import za.co.woolworths.financial.services.android.util.KotlinUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAgainScreen(
    viewModel: OrderAgainViewModel,
    onBackPressed: () -> Unit,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    val state by viewModel.orderAgainUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.onScreenEvent.collect { onScreenEvent ->
            when (onScreenEvent) {
                is OrderAgainScreenEvents.HideBottomBar -> onEvent(onScreenEvent)
                is OrderAgainScreenEvents.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.add_to_cart).uppercase(),
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }

                else -> {}
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
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { data ->
                SnackbarView(data, state.itemsToBeAddedCount, state.maxItemLimit) {
                    onEvent(OrderAgainScreenEvents.SnackbarViewClicked)
                }
            }
        }
    ) {

        OrderAgainStatelessScreen(Modifier.padding(it), state, viewModel.orderList) { event ->
            when (event) {
                OrderAgainScreenEvents.DeliveryLocationClick -> onEvent(event)
                OrderAgainScreenEvents.StartShoppingClicked -> onEvent(event)
                else -> viewModel.onEvent(event)
            }
        }
    }
}

@Composable
fun SnackbarView(
    data: SnackbarData? = null,
    count: Int,
    maxItem: Int,
    onClick: () -> Unit
) {
    Snackbar(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .height(40.dp),
        containerColor = SnackbarBackground,
        contentColor = White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FuturaTextH10(
                modifier = Modifier
                    .size(24.dp)
                    .background(White, RoundedCornerShape(12.dp))
                    .wrapContentHeight(),
                text = count.toString(),
                textAlign = TextAlign.Center
            )
            SpacerWidthDp(width = 12.dp, Color.Transparent)
            Column(Modifier.weight(1f)) {
                FuturaTextH12(
                    text = pluralStringResource(id = R.plurals.plural_add_to_cart, count, count),
                    color = White
                )
                if (KotlinUtils.isDeliveryOptionDash()) {
                    SpacerHeight6dp(bgColor = Color.Transparent)
                    FuturaTextH8(
                        text = stringResource(
                            id = R.string.dash_item_limit_message,
                            maxItem
                        ).uppercase(),
                        color = White
                    )
                }
            }
            FuturaTextH12(
                Modifier.clickable {
                    onClick()
                },
                fontWeight = FontWeight.W600,
                color = White,
                text = stringResource(id = R.string.view)
            )
        }
    }
}

@Composable
private fun OrderAgainStatelessScreen(
    modifier: Modifier = Modifier,
    state: OrderAgainUiState,
    orderList: List<ProductItem>,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    Column(modifier.background(OneAppBackground)) {
        DeliveryLocationView(state.deliveryState) {
            onEvent(OrderAgainScreenEvents.DeliveryLocationClick)
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

            OrderAgainScreenState.ShowEmptyScreen -> EmptyScreen(Modifier.background(White)) {
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
                BlackButton(
                    Modifier.fillMaxWidth(),
                    text = pluralStringResource(
                        id = R.plurals.plural_add_to_cart,
                        state.itemsToBeAddedCount,
                        state.itemsToBeAddedCount
                    )
                ) {
                    onEvent(OrderAgainScreenEvents.AddToCartClicked)
                }
                UnderlineButton(
                    Modifier.fillMaxWidth(),
                    text = stringResource(id = state.resIdCopyToList),
                    texColor = Black
                ) {
                    onEvent(OrderAgainScreenEvents.CopyToListClicked)
                }
            }
        }
    }
}

@Composable
fun DeliveryLocationView(
    deliveryState: DeliveryLocationViewState,
    onClick: () -> Unit
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
                onClick()
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
                onClick()
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
                            .background(White),
                        icon = R.drawable.cart_icon,
                        textStyle = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp,
                            color = Black
                        ),
                        actionText = R.string.add_to_cart
                    ) {
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
                    contentDescription = null
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
                textDecoration = TextDecoration.Underline
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
                painter = painterResource(id = R.drawable.ic_option_menu),
                contentDescription = ""
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
                    rightIconEnabled = productItem.quantity < productItem.quantityInStock,
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
    rightIcon: Int = R.drawable.add_black,
    rightIconEnabled: Boolean = false,
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
        CircleIcon(leftIcon, leftIconEnabled, ShimmerColor) {
            onLeftIconClick()
        }
        SpacerWidth8dp()
        FuturaTextH15(
            modifier = Modifier.widthIn(min = 24.dp),
            text = productItem.quantity.toString()
        )
        SpacerWidth8dp()
        CircleIcon(rightIcon, rightIconEnabled, ShimmerColor) {
            onRightIconClick()
        }
    }
}

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
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
            painter = painterResource(id = R.drawable.empty_list_icon),
            contentDescription = stringResource(id = R.string.cd_empty_state_icon)
        )

        SpacerHeight24dp()
        FuturaTextH1(
            modifier = Modifier.padding(horizontal = 54.dp),
            text = stringResource(id = R.string.order_again_empty_title),
            textAlign = TextAlign.Center
        )
        SpacerHeight8dp()
        OpenSansText14(
            modifier = Modifier.padding(horizontal = 54.dp),
            text = stringResource(id = R.string.order_again_empty_desc),
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
        OrderAgainStatelessScreen(state = OrderAgainUiState(), orderList = emptyList()) {}
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
private fun PreviewSnackbarView() {
    OneAppTheme {
        SnackbarView(data = null, count = 2, maxItem = 0) {

        }
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
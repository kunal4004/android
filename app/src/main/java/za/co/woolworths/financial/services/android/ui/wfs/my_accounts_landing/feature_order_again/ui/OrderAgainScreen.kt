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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH14
import za.co.woolworths.financial.services.android.presentation.common.FuturaTextH15
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewEvent
import za.co.woolworths.financial.services.android.presentation.common.OpenSansText14
import za.co.woolworths.financial.services.android.presentation.common.OpenSansTitleText10
import za.co.woolworths.financial.services.android.presentation.common.OpenSansTitleText13
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationViewState
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight12dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight40dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth8dp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainUiState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD0021B
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorFCF0F1
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorLabel
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.ShimmerColor
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAgainScreen(
    viewModel: OrderAgainViewModel,
    onBackPressed: () -> Unit,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    val state by viewModel.orderAgainUiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                SpacerHeight16dp()
                HeaderView(
                    modifier = Modifier.background(White),
                    headerViewState = state.headerState
                ) { event ->
                    when(event) {
                        HeaderViewEvent.IconClick -> onBackPressed()
                        HeaderViewEvent.RightButtonClick -> viewModel.onEvent(OrderAgainScreenEvents
                            .SelectAllClick)
                    }
                }
                SpacerHeight16dp()
                Divider(color = ColorD8D8D8)
            }
        }
    ) {

        OrderAgainStatelessScreen(Modifier.padding(it), state) { event ->
            when (event) {
                OrderAgainScreenEvents.DeliveryLocationClick -> onEvent(event)
                else -> viewModel.onEvent(event)
            }
        }
    }
}

@Composable
private fun OrderAgainStatelessScreen(
    modifier: Modifier = Modifier,
    state: OrderAgainUiState,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    Box(modifier.background(OneAppBackground)){

        Column {
            DeliveryLocationView(state.deliveryState) {
                onEvent(OrderAgainScreenEvents.DeliveryLocationClick)
            }
            SpacerHeight8dp(bgColor = OneAppBackground)
            when (state.screenState) {
                OrderAgainScreenState.Loading -> {}
                OrderAgainScreenState.ShowEmptyScreen -> EmptyScreen(Modifier.background(White))
                OrderAgainScreenState.ShowOrderList -> {
                    OrderAgainList(state.orderList.toMutableList(), onEvent)
                }

                else -> {}
            }
        }
        if(state.showAddToCart) {
            Column(Modifier.background(White)) {
                BlackButton(
                    text = pluralStringResource(
                        id = R.plurals.plural_add_to_cart,
                        state.itemsToBeAddedCount,
                        state.itemsToBeAddedCount)
                ) {
                    onEvent(OrderAgainScreenEvents.AddToCartClicked)
                }
                UnderlineButton(text = stringResource(id = state.resIdCopyToList)) {
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
            OpenSansTitleText13(text = deliveryState.textDeliveryLocation)
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
    orderList: List<ProductItem>,
    onEvent: (OrderAgainScreenEvents) -> Unit
) {
    val state = rememberLazyListState()

    LazyColumn(state = state) {
        items(orderList) {

            ProductItemView(it, onEvent = onEvent)

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
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(ColorFCF0F1)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = productItem.promotionalText,
                style = TextStyle(
                    fontFamily = FuturaFontFamily,
                    color = ColorD0021B,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W500,
                    textDecoration = TextDecoration.Underline
                )
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
    rightIcon: Int = R.drawable.add_black,
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
        CircleIcon(leftIcon, ShimmerColor) {
            onLeftIconClick()
        }
        SpacerWidth8dp()
        FuturaTextH15(
            modifier = Modifier.widthIn(min = 24.dp),
            text = productItem.quantity.toString()
        )
        SpacerWidth8dp()
        CircleIcon(rightIcon, ShimmerColor) {
            onRightIconClick()
        }
    }
}

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier
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

        }
    }

}


@Preview
@Composable
private fun PreviewOrderAgainScreen() {
    OneAppTheme {
        OrderAgainStatelessScreen(state = OrderAgainUiState()) {}
        OrderAgainList(emptyList()) {}
    }
}

@Preview
@Composable
private fun PreviewEmptyScreen() {
    OneAppTheme {
        EmptyScreen(Modifier.background(White))
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
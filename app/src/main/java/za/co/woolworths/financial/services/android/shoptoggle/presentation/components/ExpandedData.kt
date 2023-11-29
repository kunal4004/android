package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.awfs.coordination.R
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.switchBrowseModeEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedData(
    isExpanded: Boolean,
    isDefaultSelectedDelivery: Boolean,
    isAutoNavigated: Boolean,
    item: ToggleModel,
    viewModel: ShopToggleViewModel,
    onSelectDeliveryType: (Delivery?, Boolean) -> Unit
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showLearnMoreBottomSheet by remember { mutableStateOf(false) }
    var showSetLocationBottomSheet by remember { mutableStateOf(false) }

    if (isExpanded) {
        switchBrowseModeEvent(item.deliveryType)
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
        Divider(color = ColorD8D8D8, thickness = Dimens.oneDp)
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryTypeLabel,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
        when (item.deliveryType.lowercase()) {
            Delivery.DASH.type.lowercase() -> LoadDashDetails(item = item)
            Delivery.CNC.type.lowercase() -> LoadCncDetails(item = item)
            Delivery.STANDARD.type.lowercase() -> LoadStandardDetails(item = item)
            else -> {}
        }


        Spacer(modifier = Modifier.height(Dimens.eight_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryCostLabel,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )

        Text(
            modifier = Modifier
                .clickable(true) {
                    viewModel.getLearnMoreList()
                    showLearnMoreBottomSheet = true
                },
            textAlign = TextAlign.Start,
            text = buildAnnotatedString {
                append(item.deliveryCost)
                append(" ")
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,

                        )
                ) {
                    append(stringResource(R.string.fulfillment_learn_more))
                }
            },
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = Dimens.thirteen_sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))

        if (!isDefaultSelectedDelivery || isAutoNavigated) {
            BlackButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.fifty_dp),
                text = getPrimaryCtaText(isDefaultSelectedDelivery, isAutoNavigated, item).uppercase(),
                enabled = true,
            ) {
                val placeId = KotlinUtils.getDeliveryType()?.address?.placeId
                if (placeId.isNullOrEmpty()) {
                    showSetLocationBottomSheet = true
                } else {
                    val deliveryType = Delivery.getType(item.deliveryType)
                    if (isDefaultSelectedDelivery && isAutoNavigated) {
                        // Do not call the API, just send user back to the previous page
                        onSelectDeliveryType(deliveryType, false)
                    } else {
                        // Call the confirm location API
                        onSelectDeliveryType(deliveryType, true)
                    }
                }
            }
        }
    }
    if (showLearnMoreBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showLearnMoreBottomSheet = false
            },
            sheetState = sheetState,
            containerColor = Color.White,
            contentColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(Dimens.dp24),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.our_delivery_costs),
                    style = TextStyle(
                        fontFamily = FuturaFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = Dimens.twenty_sp,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
                Divider(color = ColorD8D8D8, thickness = Dimens.oneDp)
                Spacer(modifier = Modifier.height(Dimens.dp24))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimens.sixteen_dp)
                ) {
                    items(viewModel.listItemLearnMore.value) { items ->

                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                        ) {
                            LearnMoreBottomSheetListItem(items)
                        }
                    }

                }
                Spacer(modifier = Modifier.height(Dimens.dp40))
                BlackButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.fifty_dp),
                    text = stringResource(id = R.string.got_it),
                    enabled = true,
                ) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showLearnMoreBottomSheet = false
                        }
                    }
                }
            }
        }
    }

    if (showSetLocationBottomSheet) {
        val delivery = viewModel.deliveryType()
        SetLocationBottomSheetMain(
            delivery = delivery,
            onDismissRequest = {
                showSetLocationBottomSheet = false
            }
        )
    }
}

@Composable
private fun getPrimaryCtaText(isDefaultSelectedDelivery: Boolean, isAutoNavigated: Boolean, item: ToggleModel): String {
    return if (isDefaultSelectedDelivery && isAutoNavigated) {
        item.deliveryButtonTextContinue
    } else {
        item.deliveryButtonText
    }

}

@Composable
private fun LoadStandardDetails(item: ToggleModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = fbhDeliverySlotsForStandard(item),
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )

        if (!item.dataFailure) {
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Start,
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
                        append(stringResource(id = R.string.food_toggle_fulfilment))
                    }
                    append(" ")
                    append(item.deliverySlotFood)
                    withStyle(
                        style = SpanStyle(
                            fontStyle = FontStyle.Italic,
                        )
                    ) {
                        append(" ")
                        append(stringResource(id = R.string.unlimited_items))
                    }
                },
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = Dimens.thirteen_sp,
                    color = Color.Black
                )
            )
        }
    }
}

@Composable
private fun LoadDashDetails(item: ToggleModel) {
    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = dashDeliverySlots(item),
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = Dimens.thirteen_sp,
            color = Color.Black
        )
    )

    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = buildAnnotatedString {
            append(stringResource(id = R.string.food_only_toggle_fulfilment))
            append(" ")
            withStyle(
                style = SpanStyle(
                    fontStyle = FontStyle.Italic,
                )
            ) {
                append(stringResource(id = R.string.limited_food_item_msg, item.foodQuantity.toString()))
            }
        },
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = Dimens.thirteen_sp,
            color = Color.Black
        )
    )
}

@Composable
private fun LoadCncDetails(item: ToggleModel) {
    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = cncDeliverySlots(item),
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = Dimens.thirteen_sp,
            color = Color.Black
        )
    )

    if (!item.dataFailure) {

        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                    )
                ) {
                    append(stringResource(id = R.string.food_toggle_fulfilment))
                }
                append(" ")
                append(item.deliverySlotFood)
            },
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
    }
}

@Composable
private fun cncDeliverySlots(item: ToggleModel) =
    buildAnnotatedString {
        if (item.dataFailure) {
            append(stringResource(id = R.string.cnc_delivery_slots_not_available))
        } else {
            withStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                )
            ) {
                append(stringResource(id = R.string.fashion_beauty_home_toggle_fulfilment))
            }
            append(" ")
            append(item.deliverySlotFbh)
        }
    }

@Composable
private fun dashDeliverySlots(item: ToggleModel) =
    if (!item.dataFailure) {
        item.deliverySlotFood
    } else {
        stringResource(id = R.string.dash_delivery_slots_not_available)
    }

@Composable
private fun fbhDeliverySlotsForStandard(item: ToggleModel) =
    buildAnnotatedString {
        if (item.dataFailure) {
            append(stringResource(id = R.string.dash_delivery_slots_not_available))
        } else {
            withStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                )
            ) {
                append(stringResource(id = R.string.fashion_beauty_home_toggle_fulfilment))
            }
            append(" ")
            append(item.deliverySlotFbh)
        }
    }
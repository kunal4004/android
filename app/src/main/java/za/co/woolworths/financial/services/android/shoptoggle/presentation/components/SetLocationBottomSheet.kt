package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.awfs.coordination.R
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLocationBottomSheetMain(
    onDismissRequest: () -> Unit = {}, delivery: Delivery = Delivery.STANDARD
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val activity = (LocalContext.current as? ShopToggleActivity)
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
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

            SetLocationBottomSheet(delivery = delivery, onSetLocation = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                        onEditDeliveryLocation(activity = activity, delivery = delivery)
                    }
                }
            }, onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            })
        }
    }
}

@Preview
@Composable
private fun SetLocationBottomSheet(
    onSetLocation: () -> Unit = {},
    onDismiss: () -> Unit = {},
    delivery: Delivery = Delivery.STANDARD,
) {
    val learnMoreItem = getLocationModal(delivery)
    Spacer(modifier = Modifier.width(Dimens.sixteen_dp))
    Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = learnMoreItem.icon),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(Dimens.thirty_two_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            text = learnMoreItem.title,
            style = TextStyle(
                fontSize = Dimens.twenty_sp,
                lineHeight = Dimens.twenty_six_sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
            )
        )
        Spacer(modifier = Modifier.height(Dimens.eight_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            text = learnMoreItem.description,
            style = TextStyle(
                fontSize = Dimens.fourteen_sp,
                lineHeight = Dimens.twenty_sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color444444,
                textAlign = TextAlign.Center,
            )
        )
        Spacer(modifier = Modifier.height(Dimens.thirty_one_dp))
        BlackButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.fifty_dp),
            text = stringResource(id = R.string.cta_set_my_location).uppercase(),
            enabled = true,
        ) {
            onSetLocation()
        }

        UnderlineButton(
            text = learnMoreItem.secondaryCtaText
        ) {
            onDismiss()
        }
    }
}

private fun onEditDeliveryLocation(activity: ShopToggleActivity?, delivery: Delivery?) {

    KotlinUtils.presentEditDeliveryGeoLocationActivity(
        activity,
        BundleKeysConstants.REQUEST_CODE,
        Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
            ?: KotlinUtils.browsingDeliveryType,
        KotlinUtils.getDeliveryType()?.address?.placeId ?: "",
        isFromNewToggleFulfilmentScreen = true,
        newDelivery = delivery
    )
    activity?.finish()
}

private fun icon(delivery: Delivery): Int {
    return when (delivery) {
        Delivery.DASH -> R.drawable.ic_dashscooter_location_modal
        Delivery.CNC -> R.drawable.ic_collection_bag_location_modal
        else -> R.drawable.ic_delivery_truck_location_modal
    }
}

@Composable
private fun title(delivery: Delivery): String {
    return when (delivery) {
        Delivery.DASH -> stringResource(id = R.string.title_dash_set_location_modal)
        Delivery.CNC -> stringResource(id = R.string.title_cnc_set_location_modal)

        else -> stringResource(id = R.string.title_standard_set_location_modal)
    }
}

@Composable
private fun description(delivery: Delivery): String {
    return when (delivery) {
        Delivery.DASH -> stringResource(id = R.string.desc_dash_set_location_modal)
        Delivery.CNC -> stringResource(id = R.string.desc_cnc_set_location_modal)
        else -> stringResource(id = R.string.desc_standard_set_location_modal)
    }
}

@Composable
private fun secondaryCtaText(delivery: Delivery): String {
    return when (delivery) {
        Delivery.DASH -> stringResource(id = R.string.dismiss).uppercase()
        Delivery.CNC -> stringResource(id = R.string.dismiss).uppercase()
        else -> stringResource(id = R.string.voc_survey_skip).uppercase()
    }
}

@Composable
private fun getLocationModal(delivery: Delivery): LocationModal {
    return LocationModal(
        icon = icon(delivery),
        title = title(delivery),
        description = description(delivery),
        secondaryCtaText = secondaryCtaText(delivery)
    )
}

private data class LocationModal(
    val icon: Int, val title: String, val description: String, val secondaryCtaText: String
)


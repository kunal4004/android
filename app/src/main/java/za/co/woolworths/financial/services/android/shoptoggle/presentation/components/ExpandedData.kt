package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.awfs.coordination.R
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedData(
    isExpanded: Boolean,
    item: ToggleModel,
    viewModel: ShopToggleViewModel,
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val activity = (LocalContext.current as? ShopToggleActivity)
    if (isExpanded) {
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
        Divider(color = ColorD8D8D8, thickness = Dimens.oneDp)
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryType,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryTime,
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
            text = item.deliveryProduct,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(Dimens.eight_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryCost,
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
                showBottomSheet = true
            },
            textAlign = TextAlign.Start,
            text = buildAnnotatedString {
                append(item.learnMore)
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline,

                )) {
                    append(stringResource(R.string.learn_more))
                }
            },
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = Dimens.thirteen_sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))

        BlackButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.fifty_dp),
            text = item.deliveryButtonText.uppercase(),
            enabled = true,
        ) {
            when (item.id) {
                1 -> activity?.finish()
                2 -> onEditDeliveryLocation(activity)
                3 -> "//TODO: open click and collect screen"
            }
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
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
                            showBottomSheet = false
                        }
                    }
                }
            }
        }
    }
}


private fun onEditDeliveryLocation(activity: ShopToggleActivity?) {

    KotlinUtils.presentEditDeliveryGeoLocationActivity(
        activity,
        BundleKeysConstants.REQUEST_CODE,
        Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType) ?: KotlinUtils.browsingDeliveryType,
        KotlinUtils.getDeliveryType()?.address?.placeId ?: ""
    )
    activity?.finish()
}


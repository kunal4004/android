package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.ShopToggleUseCase
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.util.wenum.Delivery


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableListItem(
    item: ToggleModel,
    expandedId: Int?,
    isAutoNavigated: Boolean,
    defaultSelectionId: Int?,
    isUserAuthenticated: Boolean,
    onItemClick: () -> Unit,
    onSelectDeliveryType: (Delivery?, Boolean) -> Unit,
    viewModel: ShopToggleViewModel,
) {

    val cardShape = RoundedCornerShape(Dimens.eight_dp)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        modifier = Modifier
            .fillMaxSize()
            .border(
                BorderStroke(if (showBorder(expandedId, defaultSelectionId, item, isAutoNavigated, isUserAuthenticated)) Dimens.oneDp else Dimens.point_five_dp,
                    color = if (showBorder(expandedId, defaultSelectionId, item, isAutoNavigated, isUserAuthenticated)) Color.Black else ColorD8D8D8),
                shape = RoundedCornerShape(Dimens.four_dp)
            )
            .shadow(
                shape = cardShape,
                spotColor = ColorD8D8D8,
                elevation = Dimens.four_dp
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = cardShape,
        onClick = {
            onItemClick()
        }

    ) {
        ExpandableCard(item, isExpanded(expandedId, item, isAutoNavigated, isUserAuthenticated), defaultSelectionId == item.id, isAutoNavigated, viewModel, onSelectDeliveryType)
    }
}

private fun showBorder(isExpandedId: Int?, lastSelectionId: Int?, item: ToggleModel, isAutoNavigated: Boolean, isUserAuthenticated: Boolean): Boolean {
    return isExpanded(isExpandedId, item = item, isAutoNavigated = isAutoNavigated, isUserAuthenticated) || (lastSelectionId == item.id && (isExpandedId == null || isExpandedId == lastSelectionId))
}

private fun isExpanded(expandedId: Int?, item: ToggleModel, isAutoNavigated: Boolean, isUserAuthenticated: Boolean): Boolean {
    var expId = expandedId
    return  if (isAutoNavigated && isUserAuthenticated) {
        if (expId == null) {
            expId = ShopToggleUseCase.STANDARD_DELIVERY_ID
        }
        expId == item.id
    } else {
        expId == item.id
    }
}

@Composable
private fun ExpandableCard(
    item: ToggleModel,
    isExpanded: Boolean,
    isSelected: Boolean,
    isAutoNavigated: Boolean,
    viewModel: ShopToggleViewModel,
    onSelectDeliveryType: (Delivery?, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.sixteen_dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically

        ) {

            Image(
                painter = painterResource(id = item.icon),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(Dimens.eight_dp))

            Column(modifier = Modifier
                .weight(8f)) {
                if (!item.isDashDelivery) {
                    item.title?.let {
                        Text(
                            modifier = Modifier,
                            textAlign = TextAlign.Start,
                            text = it.uppercase(),
                            style = TextStyle(
                                fontFamily = FuturaFontFamily,
                                fontWeight = FontWeight.W500,
                                fontSize = Dimens.ten_sp,
                                color = Color.Black
                            ),
                            letterSpacing = Dimens.one_point_five_sp
                        )
                    }
                } else {
                    Box(modifier = Modifier
                        .width(Dimens.one_twenty_dp)
                        .height(Dimens.eighteen_dp)
                        .background(ColorFEE600, shape = RoundedCornerShape(Dimens.four_dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        item.title?.let {
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = it.uppercase(),
                                style = TextStyle(
                                    fontFamily = FuturaFontFamily,
                                    fontWeight = FontWeight.W600,
                                    fontSize = Dimens.ten_sp,
                                    color = Color.Black
                                )
                            )
                        }

                    }

                }
                if (item.isDashDelivery) {
                    Spacer(modifier = Modifier.height(Dimens.eight_dp))
                }
                item.subTitle?.let {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Start,
                        text = it.uppercase(),
                        style = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = Dimens.fourteen_sp,
                            color = Color.Black
                        ),
                        letterSpacing = Dimens.one_point_five_sp
                    )
                }

            }
            Image(
                modifier = Modifier
                    .weight(1f),
                alignment = Alignment.CenterEnd,
                painter = if (isExpanded) painterResource(id = R.drawable.ic_up_arrow)
                else painterResource(id = R.drawable.ic_dwon_arrow),
                contentDescription = stringResource(R.string.down_arrow))

        }

        ExpandedData(isExpanded, isSelected, isAutoNavigated, item,viewModel, onSelectDeliveryType)

    }


}



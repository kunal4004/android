package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.awfs.coordination.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetDetails
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetsUIEvents
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color9D9D9D
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */

@Composable
fun MatchingSetMainView(
    modifier: Modifier = Modifier,
    matchingSetData: MatchingSetData,
    isSeeMore: StateFlow<Boolean>,
    onEvent: (event: MatchingSetsUIEvents) -> Unit,
) {
    if (!matchingSetData.matchingSetDetails.isNullOrEmpty()) {
        val seeMoreClicked = isSeeMore.collectAsState()
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            SpacerHeight8dp(bgColor = colorResource(id = R.color.default_background))
            MatchingSetHeaderView(
                modifier,
                onEvent = {
                    onEvent(it)
                },
                if (seeMoreClicked.value) R.string.matching_set_see_less_button_text else R.string.matching_set_see_more_button_text
            )
            Column(
                modifier = modifier.wrapContentHeight(),
            ) {
                matchingSetData.matchingSetDetails.forEachIndexed { index, listItem ->
                    if (seeMoreClicked.value || (!seeMoreClicked.value && index < matchingSetData.noOfProductsToShow)) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.twenty_four_dp)),
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .height(dimensionResource(id = R.dimen.hundred_and_twelve_dp))
                                    .width(dimensionResource(id = R.dimen.eighty_dp)),
                                model = matchingSetData.matchingSetDetails.getOrNull(index)?.imgUrl,
                                placeholder = painterResource(id = R.drawable.placeholder_product_list),
                                error = painterResource(id = R.drawable.placeholder_product_list),
                                contentDescription = stringResource(id = R.string.matching_setImg_main_view),
                            )
                            Column(
                                modifier = Modifier
                                    .padding(
                                        start = dimensionResource(id = R.dimen.sixteen_dp),
                                        top = dimensionResource(id = R.dimen.five_dp)
                                    )
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = listItem.productName,
                                    style = TextStyle(
                                        fontFamily = OpenSansFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.W400,
                                        textAlign = TextAlign.Start,
                                        color = Color.Black,
                                        lineHeight = 19.5.sp
                                    )
                                )
                                Text(
                                    text = matchingSetData.matchingSetDetails.getOrNull(index)?.colorName
                                        ?: "",
                                    style = TextStyle(
                                        fontFamily = OpenSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.W600,
                                        textAlign = TextAlign.Start,
                                        color = Color9D9D9D,
                                        lineHeight = 15.sp
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = dimensionResource(id = R.dimen.thirty_dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, true),
                                        text = matchingSetData.matchingSetDetails.getOrNull(index)?.price
                                            ?: "",
                                        style = TextStyle(
                                            fontFamily = FuturaFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W600,
                                            textAlign = TextAlign.Start,
                                            color = Color.Black,
                                            lineHeight = 21.sp
                                        )
                                    )
                                    Image(
                                        modifier = Modifier
                                            .clickable { },
                                        painter = painterResource(id = R.drawable.ic_add_circle),
                                        contentDescription = stringResource(id = R.string.matching_setplus_button)
                                    )
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(color = colorResource(id = R.color.color_D8D8D8))
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchingSetMainViewPreview() {
    OneAppTheme {
        val matchingSetDetailsList = ArrayList<MatchingSetDetails>()
        for (i in 0..3) {
            val imgUrl =
                "https://assets.woolworthsstatic.co.za/Bowl-Set-4-Pack-507106238.jpg?V=k@lx&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIzLTA3LTIxLzUwNzEwNjIzOF9YQkxVRV9oZXJvLmpwZyJ9&"
            val styleId = "102865767"
            val colorName = "Red"
            val price = "R 499.00"
            val productName = "Nordic Stoneware Dinner Plate"
            val matchingSetDetails =
                MatchingSetDetails(imgUrl, styleId, colorName, price, productName)
            matchingSetDetailsList.add(matchingSetDetails)
        }
        val isSeeMore = MutableStateFlow(false)
        val matchingSetData =
            MatchingSetData(matchingSetDetailsList, 2)
        MatchingSetMainView(
            Modifier.background(color = White),
            matchingSetData,
            isSeeMore,
            onEvent = {})
    }
}
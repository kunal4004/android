package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
import za.co.woolworths.financial.services.android.models.dto.RelatedProducts
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
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
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f, true),
                text = stringResource(id = R.string.matching_set_title),
                style = TextStyle(
                    fontFamily = FuturaFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Start,
                    color = Color.Black,
                    lineHeight = 27.sp
                )
            )
            Text(
                text = stringResource(id = R.string.matching_set_see_more_button_text),
                style = TextStyle(
                    fontFamily = FuturaFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.End,
                    color = Color.Black,
                    lineHeight = 18.sp,
                    letterSpacing = 1.sp
                )
            )
        }
        LazyColumn(
            state = rememberLazyListState(),
            modifier = modifier,
        ) {
            itemsIndexed(matchingSetData.relatedProducts, key = { _, item ->
                item.productId
            }) { index, listItem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(24.dp),
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .height(112.dp)
                                .width(80.dp)
                                .fillMaxHeight(),
                            model = matchingSetData.imgUrlList[index],
                            placeholder = painterResource(id = R.drawable.placeholder_product_list),
                            error = painterResource(id = R.drawable.placeholder_product_list),
                            contentDescription = stringResource(id = R.string.matching_setImg_main_view),
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 5.dp)
                                .fillMaxHeight()
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
                                text = matchingSetData.colorNameList[index],
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
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(top = 30.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f, true),
                                    text = matchingSetData.priceList[index],
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

@Preview(showBackground = true)
@Composable
fun MatchingSetMainView() {
    OneAppTheme {
        val relatedProducts1 =
            RelatedProducts("Bowl Set 4 Pack", "507106238", null, "", "", "", "", null)
        val relatedProducts2 =
            RelatedProducts("Nordic Stoneware Bowl", "507106239", null, "", "", "", "", null)
        val relatedProducts3 =
            RelatedProducts(
                "Nordic Stoneware Dinner Plate",
                "507106237",
                null,
                "",
                "",
                "",
                "",
                null
            )
        val relatedProductsList = ArrayList<RelatedProducts>()
        relatedProductsList.add(relatedProducts1)
        relatedProductsList.add(relatedProducts2)
        relatedProductsList.add(relatedProducts3)

        val imgUrlList = ArrayList<String>()
        val styleIdList = ArrayList<String>()
        val colorNameList = ArrayList<String>()
        val priceList = ArrayList<String>()
        for (i in relatedProductsList) {
            imgUrlList.add("https://assets.woolworthsstatic.co.za/Bowl-Set-4-Pack-507106238.jpg?V=k@lx&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIzLTA3LTIxLzUwNzEwNjIzOF9YQkxVRV9oZXJvLmpwZyJ9&")
            styleIdList.add("102865767")
            colorNameList.add("Red")
            priceList.add("R 499.00")
        }
        val matchingSetData =
            MatchingSetData(relatedProductsList, imgUrlList, styleIdList, colorNameList, priceList)
        MatchingSetMainView(Modifier.background(color = White), matchingSetData)
    }
}
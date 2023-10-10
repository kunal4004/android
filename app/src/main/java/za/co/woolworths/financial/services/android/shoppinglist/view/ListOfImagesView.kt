package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ProductListDetails
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

/**
 * Created by Kunal Uttarwar on 03/10/23.
 */

@Composable
fun ListOfImagesView(
    listItem: ShoppingList,
    onImageItemClick: () -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .clickable {
                onImageItemClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.Start
        ),
        state = rememberLazyListState(),
    ) {

        val maxProductsCountInRow = listItem.noOfProductInRow - 1
        itemsIndexed(listItem.productImageList) { index, listImages ->
            if (maxProductsCountInRow > index + 1) {
                AsyncImage(
                    modifier = Modifier
                        .height(64.dp)
                        .width(54.17.dp),
                    model = listImages.imgUrl,
                    placeholder = painterResource(id = R.drawable.placeholder_product_list),
                    error = painterResource(id = R.drawable.placeholder_product_list),
                    contentDescription = stringResource(id = R.string.description),
                )
            } else if (maxProductsCountInRow == index + 1) {
                Text(
                    modifier = Modifier
                        .height(64.dp)
                        .width(51.17.dp)
                        .background(color = colorResource(id = R.color.color_F3F3F3))
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .wrapContentWidth(align = Alignment.CenterHorizontally),
                    style = TextStyle(
                        fontFamily = OpenSansFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.color_666666),
                    ),
                    textAlign = TextAlign.Center,
                    text = "+" + (listItem.listCount - maxProductsCountInRow + 1),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListOfImagesViewPreview() {
    OneAppTheme {
        val mockList = ShoppingList().apply {
            listId = "1"
            listName = "Test"
            listCount = 14
            modifiedListCount = "(14)"
            noOfProductInRow = 7
            val productListDetails = ProductListDetails().apply {
                imgUrl =
                    "https://assets.woolworthsstatic.co.za/Mini-Ginger-Cookies-30-g-6009182707657.jpg?V=kb1C&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDE4LTEwLTExLzYwMDkxODI3MDc2NTdfaGVyby5qcGcifQ&"
            }

            val mockListDetails = ArrayList<ProductListDetails>().apply {
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
                add(productListDetails)
            }
            productImageList = mockListDetails
        }

        ListOfImagesView(mockList, onImageItemClick = {})
    }
}
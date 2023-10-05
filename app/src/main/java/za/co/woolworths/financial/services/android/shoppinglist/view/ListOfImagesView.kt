package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ProductListDetails
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

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
        horizontalArrangement = Arrangement.Start,
        state = rememberLazyListState(),
    ) {

        items(listItem.productImageList) { listImages ->
            AsyncImage(
                modifier = Modifier
                    .height(64.dp)
                    .width(51.dp)
                    .padding(5.dp, 0.dp, 0.dp, 5.dp),
                model = listImages.imgUrl,
                placeholder = painterResource(id = R.drawable.placeholder_product_list),
                error = painterResource(id = R.drawable.placeholder_product_list),
                contentDescription = stringResource(id = R.string.description),
            )
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
            val productListDetails = ProductListDetails().apply {
                imgUrl =
                    "https://assets.woolworthsstatic.co.za/Mini-Ginger-Cookies-30-g-6009182707657.jpg?V=kb1C&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDE4LTEwLTExLzYwMDkxODI3MDc2NTdfaGVyby5qcGcifQ&"
            }

            var mockListDetails = ArrayList<ProductListDetails>()
            mockListDetails.add(productListDetails)
            mockListDetails.add(productListDetails)
            mockListDetails.add(productListDetails)
            productImageList = mockListDetails
        }

        ListOfImagesView(mockList, onImageItemClick = {})
    }
}
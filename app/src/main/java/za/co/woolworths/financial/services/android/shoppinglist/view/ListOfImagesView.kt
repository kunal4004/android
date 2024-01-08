package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import kotlin.math.roundToInt

/**
 * Created by Kunal Uttarwar on 03/10/23.
 */

@Composable
fun ListOfImagesView(
    listItem: ShoppingList,
    onImageItemClick: () -> Unit,
) {

    val maxImageCount = fetchMaxImageCount()

    val listImages = listItem.productImageList
    LazyRow(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .clickable {
                onImageItemClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        state = rememberLazyListState(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        userScrollEnabled = false
    ) {

        if (listImages.size <= maxImageCount) {
            items(count = listImages.size) { index ->
                AsyncImage(
                    modifier = Modifier
                        .height(64.dp)
                        .width(55.dp)
                        .fillMaxHeight(),
                    model = listItem.productImageList.get(index),
                    placeholder = painterResource(id = R.drawable.placeholder_product_list),
                    error = painterResource(id = R.drawable.placeholder_product_list),
                    contentDescription = stringResource(id = R.string.description),
                )
            }
            return@LazyRow
        }

        items(count = maxImageCount) { index ->
            AsyncImage(
                modifier = Modifier
                    .height(64.dp)
                    .width(55.dp)
                    .fillMaxHeight(),
                model = listItem.productImageList.get(index),
                placeholder = painterResource(id = R.drawable.placeholder_product_list),
                error = painterResource(id = R.drawable.placeholder_product_list),
                contentDescription = stringResource(id = R.string.description),
            )
        }

        items(count = 1) { index ->
            Box (modifier = Modifier
                .height(64.dp)
                .width(55.dp)
                .background(colorResource(id = R.color.color_F3F3F3)),
                contentAlignment = Alignment.Center
            ){
                Text(
                    style = TextStyle(
                        fontFamily = OpenSansFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.color_666666),
                    ),
                    textAlign = TextAlign.Center,
                    text = "+" + (listImages.size - maxImageCount),
                )
            }
        }
    }
}

@Composable
fun fetchMaxImageCount(): Int {
    val configuration = LocalConfiguration.current
    val widthInDp = configuration.screenWidthDp.dp - 55.dp
    return widthInDp.div(55.dp).roundToInt()-1

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
            productImageList = listOf("https://assets.woolworthsstatic.co.za/Mini-Ginger-Cookies-30-g-6009182707657.jpg?V=kb1C&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDE4LTEwLTExLzYwMDkxODI3MDc2NTdfaGVyby5qcGcifQ&")
        }

        ListOfImagesView(mockList, onImageItemClick = {})
    }
}
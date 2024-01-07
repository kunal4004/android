package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.shoppinglist.component.ListDataState
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ProductListDetails
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

/**
 * Created by Kunal Uttarwar on 29/09/23.
 */

@Composable
fun MyListItemRowView(
    modifier: Modifier = Modifier,
    listDataState: ListDataState,
    listItem: ShoppingList,
    onDeleteIconClick: (item: ShoppingList) -> Unit,
    onShareIconClick: (item: ShoppingList) -> Unit,
    onDetailsArrowClick: (item: ShoppingList) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White),
        ) {
            Text(
                text = listItem.listName,
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                modifier = Modifier.padding(3.dp, 0.dp, 3.dp, 0.dp),
                text = listItem.modifiedListCount,
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(listDataState.isEditMode) {
                    Icon(painter = painterResource(id = listDataState.deleteIcon),
                        contentDescription = "Delete List",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                onDeleteIconClick(listItem)
                            }
                    )
                } else {
                    Icon(painter = painterResource(id = listDataState.shareIcon),
                        contentDescription = "Share List",
                        modifier = Modifier
                            .padding(5.dp, 0.dp, 7.dp, 0.dp)
                            .size(16.dp)
                            .clickable {
                                onShareIconClick(listItem)
                            }
                    )

                    Icon(
                        painter = painterResource(id = listDataState.openIcon),
                        contentDescription = "Open List",
                        modifier = Modifier
                            .padding(7.dp, 0.dp, 0.dp, 0.dp)
                            .clickable {
                                onDetailsArrowClick(listItem)
                            },
                    )
                }
            }
        }

        Row () {
            Text(
                text = "3 Collaborator",
                style = TextStyle(
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF666666),
                    letterSpacing = 0.22.sp,
                )
            )
        }

        if (listItem.productImageURLs.isNotEmpty()) {

            Spacer(modifier = Modifier.height(15.dp))

            ListOfImagesView(listItem, onImageItemClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyListItemRowPreview() {
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

            val mockListDetails = ArrayList<ProductListDetails>()
            mockListDetails.add(productListDetails)
            mockListDetails.add(productListDetails)
            mockListDetails.add(productListDetails)
          //  productImageList = mockListDetails
        }
        val mockListData: List<ShoppingList> = emptyList()
        mockListData.plus(mockList)
        val listData =
            ListDataState(
                mockListData, emptyList(), emptyList(), R.drawable.ic_share, R.drawable
                    .ic_white_chevron_right,
                isEditMode = true
            )
        MyListItemRowView(listDataState = listData, listItem = mockList, onShareIconClick = {},
            onDetailsArrowClick = {}, onDeleteIconClick = {})
    }
}
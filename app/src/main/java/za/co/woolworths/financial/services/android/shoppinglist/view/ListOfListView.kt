package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.shoppinglist.component.ListDataState
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight10dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

/**
 * Created by Kunal Uttarwar on 29/09/23.
 */


@Composable
fun ListOfListView(
    modifier: Modifier = Modifier,
    listDataState: ListDataState,
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier,
    ) {
        itemsIndexed(listDataState.list, key = { _, item ->
            item.listId
        }) { index, listItem ->
            MyListItemRowView(listDataState, listItem, onDetailsArrowClick = { list ->
                onEvent(MyLIstUIEvents.ListItemClick(list))
            }, onShareIconClick = { list ->
                onEvent(MyLIstUIEvents.ShareListClick(list))
            })
            if (listItem.listCount != 0) {
                // If list has no products then we don't need extra spacing.
                SpacerHeight10dp()
            }
            if (listDataState.list.size != index + 1) {
                // This condition will not show divider after last list
                Divider(
                    color = colorResource(id = R.color.color_D8D8D8)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListOfListViewPreview() {
    OneAppTheme {
        val mockList = ShoppingList().apply {
            listId = "1"
            listName = "Kunal"
            listCount = 14
            modifiedListCount = "(14)"
        }
        val mockListData: List<ShoppingList> = emptyList()
        mockListData.plus(mockList)
        val listData =
            ListDataState(mockListData, R.drawable.ic_share, R.drawable.ic_white_chevron_right)
        ListOfListView(modifier = Modifier.background(Color.White), listData, onEvent = {})
    }
}
package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight10dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

/**
 * Created by Kunal Uttarwar on 29/09/23.
 */


@Composable
fun ListOfListView(
    modifier: Modifier = Modifier,
    listDataState: ListDataState,
    onItemClick: (item: ShoppingList) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier,
    ) {
        items(listDataState.list, key = { item ->
            item.listId
        }) { listItem ->
            MyListItemRowView(listDataState, listItem) {
                onItemClick(listItem)
            }
            SpacerHeight10dp()
            Divider(
                color = colorResource(id = R.color.color_D8D8D8)
            )
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
        ListOfListView(modifier = Modifier.background(Color.White), listData, onItemClick = {})
    }
}
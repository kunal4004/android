package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.shoppinglist.component.ListDataState
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight15dp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.roundToPx
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

/**
 * Created by Kunal Uttarwar on 29/09/23.
 */

@Composable
fun ListOfListView(
    modifier: Modifier = Modifier,
    isClickedOnShareList: Boolean = false,
    listDataState: ListDataState,
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
     ShowList(modifier, isClickedOnShareList, listDataState, onEvent)
}

@Composable
private fun ShowList(
    modifier: Modifier,
    isClickedOnShareList: Boolean,
    listDataState: ListDataState,
    onEvent: (event: MyLIstUIEvents) -> Unit
) {
    val list = if (isClickedOnShareList)
        listDataState.shareList
    else
        listDataState.list

    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier,
    ) {
        itemsIndexed(
            list, key = { _, item ->
                item
            }) { index, listItem ->

            SwipeToRevealView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                rowOffsetInPx = 116.dp.roundToPx(),
                animationDurationInMillis = 600,
                isRevealed = listDataState.revealedList.contains(listItem.listId),
                onExpand = {
                    onEvent(MyLIstUIEvents.ListItemRevealed(listItem))
                },
                onCollapse = {
                    onEvent(MyLIstUIEvents.ListItemCollapsed(listItem))
                },
                rowContent = {
                    Column(
                        modifier = Modifier
                            .background(Color.White),
                    ) {
                        MyListItemRowView(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 15.dp),
                            listDataState, listItem, onDetailsArrowClick = { list ->
                                onEvent(MyLIstUIEvents.ListItemClick(list))
                            }, onShareIconClick = { list ->
                                onEvent(MyLIstUIEvents.ShareListClick(list))
                            },
                            onDeleteIconClick = {
                                onEvent(MyLIstUIEvents.OnSwipeDeleteAction(it, index))
                            })

                        if (listItem.listCount != 0 && listDataState.list.size == index + 1) {
                            // If list has no products then we don't need extra spacing.
                            SpacerHeight15dp()
                        }
                        if (listDataState.list.size != index + 1) {
                            // This condition will not show divider after last list
                            Divider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = colorResource(id = R.color.color_D8D8D8)
                            )
                        }
                    }
                },
                actionContent = {
                    SwipeListActionItem(
                        modifier = Modifier
                            .width(116.dp)
                            .fillMaxHeight()
                            .background(colorResource(id = R.color.red_color)),
                        icon = R.drawable.delete_24,
                        tintColor = Color.White,
                        textStyle = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        actionText = R.string.remove
                    ) {
                        onEvent(MyLIstUIEvents.OnSwipeDeleteAction(listItem, index))
                    }
                }
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
            ListDataState(
                mockListData,
                emptyList(),
                emptyList(),
                R.drawable.ic_share,
                R.drawable.ic_white_chevron_right
            )
        ListOfListView(modifier = Modifier.background(Color.White) , true, listData, onEvent = {})
    }
}
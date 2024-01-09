package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.viewmodel.MyListViewModel

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */

@Composable
fun MyListView(
    modifier: Modifier = Modifier,
    myListviewModel: MyListViewModel,
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
    val isProgressBarNeeded = myListviewModel.isLoading.collectAsState()
    Box(modifier = modifier) {
        MyListScreen(
            onCreateNewList = {
                onEvent(MyLIstUIEvents.CreateListClick)
            },
            onEvent = onEvent,
            myListviewModel
        )
        if (isProgressBarNeeded.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black
            )
        }
    }
}

@Composable
fun MyListScreen(
    onCreateNewList: () -> Unit,
    onEvent: (event: MyLIstUIEvents) -> Unit,
    myListviewModel: MyListViewModel,
) {
    val myListState = myListviewModel.myListState.value
    val listStateData = myListviewModel.listDataState.value
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = colorResource(id = R.color.color_D8D8D8))
        )

        if (listStateData.isError || (listStateData.list.isEmpty() && listStateData.isSuccessResponse)) {
            EmptyStateTemplate(
                myListState,
                onClickEvent = {
                    if (it) {
                        onEvent(MyLIstUIEvents.SignInClick)
                    } else {
                        onEvent(MyLIstUIEvents.CreateListClick)
                    }
                }
            )
        } else {
            CreateNewListView(icon = R.drawable.ic_add_circle, title = R.string.shop_create_list) {
                onCreateNewList()
            }

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.color_D8D8D8))
            )

            ListOfListView(
                modifier = Modifier
                    .background(Color.White),
                myListviewModel.listDataState.value,
                onEvent = {
                    when (it) {
                        is MyLIstUIEvents.ListItemClick -> onEvent(it)
                        is MyLIstUIEvents.ShareListClick -> onEvent(it)
                        is MyLIstUIEvents.OnSwipeDeleteAction -> onEvent(it)
                        else -> myListviewModel.onEvent(it)
                    }
                })
        }
    }
}
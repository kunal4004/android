package za.co.woolworths.financial.services.android.shoppinglist.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.viewmodel.MyListViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import za.co.woolworths.financial.services.android.shoppinglist.component.EmptyStateData

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

        val list = mutableListOf(
            stringResource(id = R.string.my_list_option),
            stringResource(id = R.string.share_list_option))

        var selectedIndex by remember {
            mutableStateOf(0)
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            CustomTabRow(
                list,
                selectedIndex,
                Modifier.padding(start = 24.dp, end = 24.dp)
            ) { index ->
                selectedIndex = index
            }

           if (selectedIndex == 0) {
                myListviewModel.setIsClickedOnShareLists(false)
                myListviewModel.updateEmptyScreenForList()
                MyListScreen(
                   onCreateNewList = {
                       onEvent(MyLIstUIEvents.CreateListClick)
                   }, onEvent = onEvent, myListviewModel
               )
            } else {
                myListviewModel.setIsClickedOnShareLists(true)
                myListviewModel.updateEmptyScreenForSharedList()
                MyListScreen(
                   onCreateNewList = {
                       onEvent(MyLIstUIEvents.CreateListClick)
                   }, onEvent = onEvent, myListviewModel
                )
            }
        }
            if (isProgressBarNeeded.value) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = Color.Black
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

/*        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = colorResource(id = R.color.color_D8D8D8))
        )*/

        if (listStateData.isError ||
            (listStateData.list.isEmpty() && listStateData.isSuccessResponse && !myListviewModel.isClickedOnShareLists())) {

            EmptyStateTemplate(myListState, onClickEvent = {
                if (it) {
                    onEvent(MyLIstUIEvents.SignInClick)
                } else {
                    onEvent(MyLIstUIEvents.CreateListClick)
                }
            })
        } else if (listStateData.isError ||
            (listStateData.shareList.isEmpty() && listStateData.isSuccessResponse && myListviewModel.isClickedOnShareLists())) {
            EmptyStateTemplate(myListState, onClickEvent = {

            })
        }else {
            if (!myListviewModel.isClickedOnShareLists()){
                CreateNewListView(icon = R.drawable.ic_add_circle, title = R.string.shop_create_list) {
                    onCreateNewList()
                }
            }

/*            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.color_D8D8D8))
            )*/

            ListOfListView(modifier = Modifier.background(Color.White),
                myListviewModel.isClickedOnShareLists(),
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
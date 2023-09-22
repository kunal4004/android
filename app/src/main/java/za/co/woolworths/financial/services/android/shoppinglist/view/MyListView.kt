package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoppinglist.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.viewmodel.MyListViewModel

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */

@Composable
fun MyListView(
    modifier: Modifier = Modifier,
    viewModel: MyListViewModel,
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
    Box(modifier = modifier) {
        MyListScreen(onEvent = {
            when (it) {
                is MyLIstUIEvents.CreateListClick -> onEvent(
                    MyLIstUIEvents.CreateListClick
                )

                else -> {
                }
            }
        })
    }
}

@Composable
fun MyListScreen(
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CreateNewListView(icon = R.drawable.ic_add_circle, title = R.string.shop_create_list) {

        }
    }
}
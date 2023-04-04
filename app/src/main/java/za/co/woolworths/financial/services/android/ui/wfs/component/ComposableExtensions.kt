package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground

@Composable
fun BoxBackground(content: @Composable () -> Unit) {
    Box(modifier = screenModifier.padding(top = 65.dp)) {
        content()
    }
}

@Composable
fun <T> ListColumn(
    list: MutableList<T>,
    listState: LazyListState = rememberLazyListState(),
    content: @Composable (T) -> Unit
) {
    LazyColumn(state = listState, userScrollEnabled = true, modifier = lazyColumnModifier) {
        item {}
        items(list) { item ->
                content(item)
        }
        item { Box(Modifier.fillMaxWidth().height(200.dp).background(color = OneAppBackground)) }   // vertical height added for smooth scrolling
    }
}
package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.LazyRowSnapAnimation
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyListRowSnap(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp  / 2
    LazyRowSnapAnimation(width = width) { listState ->
        LazyRow(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    bottom = Margin.bottom,
                    top = Dimens.eight_dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            content()
        }
    }
}
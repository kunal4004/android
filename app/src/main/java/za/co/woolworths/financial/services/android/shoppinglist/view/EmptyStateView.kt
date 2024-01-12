package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import za.co.woolworths.financial.services.android.shoppinglist.component.EmptyStateData
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents

/**
 * Created by Kunal Uttarwar on 11/01/24.
 */


@Composable
fun EmptyStateView(
    modifier: Modifier = Modifier,
    emptyStateData: EmptyStateData,
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
    Box(modifier = modifier) {
        EmptyStateTemplate(
            emptyStateData,
            onClickEvent = {
                if (!it) {
                    onEvent(MyLIstUIEvents.StartShoppingClick)
                }
            }
        )
    }
}
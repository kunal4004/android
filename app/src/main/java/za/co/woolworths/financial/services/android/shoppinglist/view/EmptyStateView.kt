package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.runtime.Composable
import za.co.woolworths.financial.services.android.shoppinglist.component.EmptyStateData
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents

/**
 * Created by Kunal Uttarwar on 11/01/24.
 */


@Composable
fun EmptyStateView(
    emptyStateData: EmptyStateData,
    onEvent: (event: MyLIstUIEvents) -> Unit,
) {
        EmptyStateTemplate(
            emptyStateData,
            onClickEvent = {
                if (!it) {
                    onEvent(MyLIstUIEvents.StartShoppingClick)
                }
            }
        )
}
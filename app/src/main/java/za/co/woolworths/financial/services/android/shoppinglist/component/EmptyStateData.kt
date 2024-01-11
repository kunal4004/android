package za.co.woolworths.financial.services.android.shoppinglist.component

import com.awfs.coordination.R

/**
 * Created by Kunal Uttarwar on 11/10/23.
 */

data class EmptyStateData(
    val isSignedOut: Boolean = false,
    val icon: Int = R.drawable.empty_list_icon,
    val title: Int = R.string.title_no_shopping_lists,
    val description: Int = R.string.description_no_shopping_lists,
    val buttonText: Int = R.string.button_no_shopping_lists
)
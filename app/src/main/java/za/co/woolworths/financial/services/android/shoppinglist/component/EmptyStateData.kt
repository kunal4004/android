package za.co.woolworths.financial.services.android.shoppinglist.component

import com.awfs.coordination.R

/**
 * Created by Kunal Uttarwar on 11/10/23.
 */

data class EmptyStateData(
    val isSignedOut: Boolean = false,
    val icon: Int = R.drawable.empty_list_icon,
    var title: Int = R.string.title_no_shopping_lists,
    var description: Int = R.string.description_no_shopping_lists,
    var buttonText: Int = R.string.button_no_shopping_lists,
    var isButtonVisible: Boolean = true
)
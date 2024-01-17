package za.co.woolworths.financial.services.android.shoppinglist.component

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList

/**
 * Created by Kunal Uttarwar on 29/09/23.
 */
data class ListDataState(
    val list: List<ShoppingList> = emptyList(),
    val revealedList: List<String> = emptyList(),
    val shareIcon : Int = R.drawable.ic_share,
    val openIcon: Int = R.drawable.ic_white_chevron_right,
    val deleteIcon: Int = R.drawable.ic_delete_icon_big,
    val isError: Boolean = false,
    val isSuccessResponse: Boolean = false,
    val isEditMode: Boolean = false

)

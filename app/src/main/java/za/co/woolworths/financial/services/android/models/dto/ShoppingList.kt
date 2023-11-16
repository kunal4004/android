package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ProductListDetails

@Parcelize
data class ShoppingList(
    @SerializedName("id")
    var listId: String = "",

    @SerializedName("name")
    var listName: String = "",

    @SerializedName("itemCount")
    var listCount: Int = 0,
    var shoppingListRowWasSelected: Boolean = false,
    var wasSentToServer: Boolean = false,
    var modifiedListCount: String = "",
    var productImageList: List<ProductListDetails> = emptyList() // todo This will get replace once we get actual data from response.
): Parcelable
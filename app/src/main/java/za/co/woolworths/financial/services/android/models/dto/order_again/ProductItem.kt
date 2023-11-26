package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart

@Parcelize
data class ProductItem(
    val id: String = "",
    val productName: String = "",
    val promotionalText: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val priceString: String = "",
    val wasPrice: Double = 0.0,
    val wasPriceString: String = "",
    val priceTextColor: @RawValue Color = Color.Black
) : Parcelable {

    var isSelected  by mutableStateOf(false)
    var quantityInStock by mutableStateOf(-1)
    var quantity by  mutableStateOf(1)
    var productAvailabilityResource by mutableStateOf(R.string.empty)
}

fun ProductItem.toAddItemToCart(): AddItemToCart {
    return AddItemToCart(id, id, quantity)
}
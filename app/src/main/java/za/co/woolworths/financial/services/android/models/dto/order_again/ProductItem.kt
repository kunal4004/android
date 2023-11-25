package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductItem(
    val id: String = "",
    val productName: String = "",
    val promotionalText: String = "",
    val productImage: String = "",
    val priceText: String = "",
    val price: Double = 0.0,
    val wasPrice: Double = 0.0,
) : Parcelable {
    var isSelected  by mutableStateOf(false)
    var quantityInStock by mutableStateOf(-1)
    var quantity by  mutableStateOf(1)
    var productAvailabilityResource by mutableStateOf(R.string.empty)

}

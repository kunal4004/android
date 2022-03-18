package za.co.woolworths.financial.services.android.models.dto.shop

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.ProductList

@Parcelize
data class ProductCatalogue(
    @SerializedName("name")
    var name: String? = null,

    @SerializedName("headerText")
    var headerText: String? = null,

    @SerializedName("banners")
    var banners: List<Banner>? = null,

    @SerializedName("products")
    var products: List<ProductList>? = null
) : Parcelable
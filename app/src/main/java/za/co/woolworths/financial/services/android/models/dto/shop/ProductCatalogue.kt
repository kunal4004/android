package za.co.woolworths.financial.services.android.models.dto.shop

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.ProductList

@Parcelize
data class ProductCatalogue(
    var name: String? = null,

    var headerText: String? = null,

    var banners: List<Banner>? = null,

    var products: List<ProductList>? = null
) : Parcelable
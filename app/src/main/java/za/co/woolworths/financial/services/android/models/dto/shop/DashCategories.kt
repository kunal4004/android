package za.co.woolworths.financial.services.android.models.dto.shop

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.checkout.service.network.Response
import java.util.*

@Parcelize
data class DashCategories(
    @SerializedName("productCatalogues")
    val productCatalogues: ArrayList<ProductCatalogue>? = null,

    @SerializedName("response")
    var response: Response? = null,

    @SerializedName("httpCode")
    var httpCode: Int? = null
) : Parcelable
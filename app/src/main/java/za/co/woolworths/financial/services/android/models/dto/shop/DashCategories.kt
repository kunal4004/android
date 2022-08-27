package za.co.woolworths.financial.services.android.models.dto.shop

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.checkout.service.network.Response
import java.util.*

@Parcelize
data class DashCategories(
    val productCatalogues: ArrayList<ProductCatalogue>? = null,

    var response: Response? = null,

    var httpCode: Int? = null
) : Parcelable
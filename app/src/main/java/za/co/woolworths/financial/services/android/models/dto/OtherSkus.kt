package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Parcelize
data class OtherSkus (
    @JvmField
    var sku: String? = null,

    var externalColourRef: String? = null,

    var colourImagePath: String? = null,

    @JvmField
    var price: String? = null,

    var displayName: String? = null,

    @JvmField
    var size: String? = null,

    @JvmField
    var colour: String? = null,

    var imagePath: String? = null,

    var externalImageRefV2: String? = null,

    var wasPrice: String? = null,

    @JvmField
	var quantity: Int = 0,

    var kilogramPrice: String? = null,

    var styleIdOnSale: Boolean? = null
): Parcelable
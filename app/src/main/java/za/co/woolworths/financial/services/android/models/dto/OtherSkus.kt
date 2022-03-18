package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Parcelize
data class OtherSkus (
    @JvmField
	@SerializedName("sku")
    var sku: String? = null,

    @SerializedName("externalColourRef")
    var externalColourRef: String? = null,

    @SerializedName("colourImagePath")
    var colourImagePath: String? = null,

    @JvmField
	@SerializedName("price")
    var price: String? = null,

    @SerializedName("displayName")
    var displayName: String? = null,

    @JvmField
	@SerializedName("size")
    var size: String? = null,

    @JvmField
	@SerializedName("colour")
    var colour: String? = null,

    @SerializedName("imagePath")
    var imagePath: String? = null,

    @SerializedName("externalImageRefV2")
    var externalImageRefV2: String? = null,

    @SerializedName("wasPrice")
    var wasPrice: String? = null,

    @JvmField
	var quantity: Int = 0,

    @SerializedName("kilogramPrice")
    var kilogramPrice: String? = null,

    @SerializedName("styleIdOnSale")
    var styleIdOnSale: Boolean? = null
): Parcelable
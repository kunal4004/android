package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by dimitrij on 2017/01/12.
 */
@Parcelize
data class PromotionImages(
    @SerializedName("newImage")
    var newImage: String? = null,
    @SerializedName("save")
    var save: String? = null,
    @SerializedName("vitality")
    var vitality: String? = null,
    @SerializedName("wRewards")
    var wRewards: String? = null,
    @SerializedName("freeGift")
    var freeGift: String? = null,
    @SerializedName("reduced")
    var reduced: String? = null,
    @SerializedName("wList")
    var wList: String? = null,

    ) : Parcelable
package za.co.woolworths.financial.services.android.models.dto.order_again

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Item {
    @SerializedName("_affinity")
    @Expose
    var affinity: Double? = null

    @SerializedName("slotIndex")
    @Expose
    var slotIndex: Int? = null

    @SerializedName("itemGroupId")
    @Expose
    var itemGroupId: String? = null

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("ratings")
    @Expose
    var ratings: Int? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("pattern")
    @Expose
    var pattern: String? = null

    @SerializedName("_rawAffinity")
    @Expose
    var rawAffinity: Double? = null

    @SerializedName("priceRange")
    @Expose
    var priceRange: String? = null

    @SerializedName("reviewEnabled")
    @Expose
    var reviewEnabled: String? = null

    @SerializedName("sizeCount")
    @Expose
    var sizeCount: Int? = null

    @SerializedName("reviewCount")
    @Expose
    var reviewCount: Int? = null

    @SerializedName("variant")
    @Expose
    var variant: String? = null

    @SerializedName("productClassification")
    @Expose
    var productClassification: String? = null

    @SerializedName("link")
    @Expose
    var link: String? = null

    @SerializedName("recSetId")
    @Expose
    var recSetId: Int? = null

    @SerializedName("colorCount")
    @Expose
    var colorCount: Int? = null

    @SerializedName("recToken")
    @Expose
    var recToken: String? = null

    @SerializedName("imageLink")
    @Expose
    var imageLink: String? = null

    @SerializedName("plist3620006")
    @Expose
    var plist3620006: Double? = null

    @SerializedName("plist3620006_wp")
    @Expose
    var plist3620006Wp: Int? = null

    @SerializedName("isNewImagery")
    @Expose
    var isNewImagery: Boolean? = null

    @SerializedName("isLiquor")
    @Expose
    var isLiquor: Boolean? = null

    @SerializedName("price")
    @Expose
    var price: Double? = null

    @SerializedName("badges")
    @Expose
    var badges: String? = null

    @SerializedName("badgesImgLink")
    @Expose
    var badgesImgLink: String? = null

    @SerializedName("PROMOTION")
    @Expose
    var promotion: String? = null

    @SerializedName("promotionURL")
    @Expose
    var promotionURL: String? = null

    @SerializedName("BULKPROMOTION")
    @Expose
    var bulkpromotion: String? = null

    @SerializedName("bulkPromotionURL")
    @Expose
    var bulkPromotionURL: String? = null

    @SerializedName("PROMOTION1")
    @Expose
    var promotion1: String? = null

    @SerializedName("promotion1URL")
    @Expose
    var promotion1URL: String? = null
}
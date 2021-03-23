package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Kunal Uttarwar on 15/3/21.
 */
class Promotions {
    @SerializedName("promotionalText")
    @Expose
    var promotionalText: String? = null

    @SerializedName("searchTerm")
    @Expose
    var searchTerm: String? = null
}
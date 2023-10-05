package za.co.woolworths.financial.services.android.shoppinglist.service.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Kunal Uttarwar on 04/10/23.
 */
//todo This data class might get replace with original response.
@Parcelize
data class ProductListDetails(
    var productId: String = "",
    var imgUrl: String = "",
) : Parcelable

package za.co.woolworths.financial.services.android.shoppinglist.component

import com.awfs.coordination.R

/**
 * Created by Kunal Uttarwar on 26/09/23.
 */
data class LocationDetailsState(
    val icon: Int = R.drawable.ic_delivery_circle,
    val deliveryType: String = "",
    val deliveryLocation: String = "",
)
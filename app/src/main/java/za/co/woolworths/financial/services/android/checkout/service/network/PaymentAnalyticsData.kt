package za.co.woolworths.financial.services.android.checkout.service.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Kunal Uttarwar on 13/03/23.
 */
@Parcelize
class PaymentAnalyticsData(
    @SerializedName("value")
    val value: Double? = null,

    @SerializedName("transaction_id")
    val transaction_id: String? = null,

    @SerializedName("payment_type")
    val payment_type: String? = null,

    @SerializedName("shipping")
    val shipping: Int? = null,

) : Parcelable

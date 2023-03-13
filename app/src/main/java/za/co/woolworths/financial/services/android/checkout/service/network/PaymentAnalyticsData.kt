package za.co.woolworths.financial.services.android.checkout.service.network

/**
 * Created by Kunal Uttarwar on 13/03/23.
 */
data class PaymentAnalyticsData(
    val value: Double,
    val transaction_id: String,
    val payment_type: String,
    val shipping: Int
)

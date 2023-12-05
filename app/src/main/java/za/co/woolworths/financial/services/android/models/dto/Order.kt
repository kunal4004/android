package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.JsonElement
import java.io.Serializable

data class Order(
    var completedDate: String,
    var orderCancellable: Boolean,
    var orderId: String,
    var state: String,
    var submittedDate: String,
    var total: Double,
    var taxNoteNumbers: ArrayList<String>,
    var requestCancellation: Boolean,
    var deliveryDates: JsonElement?,
    var clickAndCollectOrder: Boolean,
    var deliveryStatus: MyOrderDeliveryStatus?,
    val endlessAisleOrder: Boolean = false,
    val barcode: String = ""
) : Serializable

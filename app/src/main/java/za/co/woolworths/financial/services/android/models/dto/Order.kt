package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class Order(var completedDate: String,
                 var orderCancellable: Boolean, var orderId: String,
                 var state: String, var submittedDate: String,
                 var total: Double, var taxNoteNumbers: ArrayList<String>, var requestCancellation: Boolean) : Serializable
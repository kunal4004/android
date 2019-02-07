package za.co.woolworths.financial.services.android.models.dto

data class UpcomingOrder(var completedDate: String,
                         var orderCancellable: Boolean, var orderId: String,
                         var state: String, var submittedDate: String,
                         var total: Float, var taxNoteNumbers: ArrayList<String>)
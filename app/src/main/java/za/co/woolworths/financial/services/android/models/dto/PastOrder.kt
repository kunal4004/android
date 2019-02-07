package za.co.woolworths.financial.services.android.models.dto

class PastOrder(var completedDate: String,
                var orderCancellable: Boolean, var orderId: String,
                var state: String, var submittedDate: String,
                var total: Float, var taxNoteNumbers: ArrayList<String>)
package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class PayUPayResultRequest(val customer: String, val payment_id: String, val charge_id: String, val status: String, val productOfferingID: String) : Serializable
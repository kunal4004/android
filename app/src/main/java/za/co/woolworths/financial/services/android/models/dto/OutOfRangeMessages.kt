package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class OutOfRangeMessages(val inRange: String? = null, val outOfRange: String? = null) : Serializable
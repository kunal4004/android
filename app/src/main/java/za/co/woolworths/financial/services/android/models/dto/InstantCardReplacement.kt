package za.co.woolworths.financial.services.android.models.dto

data class InstantCardReplacement(val minSupportedAppVersion: String? = "", var isEnabled: Boolean = false, val validStoreCardBins: MutableList<Int>)
package za.co.woolworths.financial.services.android.models.dto

data class InstantCardReplacement(val minimumSupportedAppBuildNumber: Int, var isEnabled: Boolean = false, val validStoreCardBins: MutableList<Int>, val geofencing: Geofencing)
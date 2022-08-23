package za.co.woolworths.financial.services.android.util.wenum

import java.util.*

enum class Delivery(val type: String) {
    STANDARD("Standard"), CNC("CnC"), DASH("OnDemand");

    fun equals(vendor: String): Boolean {
        return type.equals(vendor, ignoreCase = true)
    }

    companion object {
        fun getType(vendor: String?): Delivery? = values()
            .find { it.type.lowercase(Locale.getDefault()) == vendor?.lowercase(Locale.getDefault()) }
    }
}
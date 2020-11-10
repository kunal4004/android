package za.co.woolworths.financial.services.android.util.wenum

import java.util.*

enum class PMAVendorCardEnum(val card: String) {
    MASTERCARD("mastercard"), VISA("visa");

    fun equals(vendor: String): Boolean {
        return card.equals(vendor, ignoreCase = true)
    }

    companion object {
        fun getCard(vendor: String?): PMAVendorCardEnum? = values().find { it.card.toLowerCase(Locale.getDefault()) == vendor?.toLowerCase(Locale.getDefault()) }
    }
}
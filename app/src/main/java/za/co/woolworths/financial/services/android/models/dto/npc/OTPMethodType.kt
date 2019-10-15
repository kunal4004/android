package za.co.woolworths.financial.services.android.models.dto.npc

enum class OTPMethodType {
    SMS, EMAIL, NONE
}

enum class LinkCardType(val type: String) {
    VIRTUAL_TEMP_CARD("VC"), LINK_NEW_CARD("SC")
}
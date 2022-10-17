package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

enum class StoreCardBlockType(val type: String?) {
    TEMPORARY("temporary"),
    PERMANENT("permanent"),
    NONE("");

    companion object {
        fun getEnum(code: String?): StoreCardBlockType? =
            values().find { it.type == code?.lowercase() }
    }
}
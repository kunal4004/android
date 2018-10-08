package za.co.woolworths.financial.services.android.models.dto

data class RefinementSelectableItem(var obj: String, var type: TYPE) {
    enum class TYPE {
        SECTION_HEADER, PROMOTION, NAVIGATOR, SINGLE_SELECTOR, MULTI_SELECTOR
    }
}
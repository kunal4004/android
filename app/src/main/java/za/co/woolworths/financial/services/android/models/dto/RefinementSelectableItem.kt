package za.co.woolworths.financial.services.android.models.dto

data class RefinementSelectableItem(var obj: String, var type: ViewType) {
    enum class ViewType(val value: Int) {
        SECTION_HEADER(1), PROMOTION(2), NAVIGATOR(3), SINGLE_SELECTOR(4), MULTI_SELECTOR(5)
    }
}
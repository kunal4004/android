package za.co.woolworths.financial.services.android.models.dto

data class RefinementSelectableItem(val item: Any?, val type: ViewType) {

    var isSelected: Boolean = false
    enum class ViewType(val value: Int) {
        SECTION_HEADER(0), PROMOTION(1), OPTIONS(2), SINGLE_SELECTOR(3), MULTI_SELECTOR(4), CATEGORY(5)
    }
}
package za.co.woolworths.financial.services.android.models.dto

class OrderItem (val item: Any?, val type: ViewType) {

    var isSelected: Boolean = false
    enum class ViewType(val value: Int) {
        UPCOMING_ORDER(0), PAST_ORDER(1), HEADER(2)
    }
}
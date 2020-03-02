package za.co.woolworths.financial.services.android.models.dto

class OrderDetailsItem(val item: Any?, val type: ViewType) {

    enum class ViewType(val value: Int) {
        ORDER_STATUS(0), ADD_TO_LIST_LAYOUT(1), HEADER(2), COMMERCE_ITEM(3), VIEW_TAX_INVOICE(4), CANCEL_ORDER(5)
    }
}
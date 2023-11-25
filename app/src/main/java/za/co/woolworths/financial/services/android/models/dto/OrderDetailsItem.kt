package za.co.woolworths.financial.services.android.models.dto

class OrderDetailsItem(val item: Any?, val type: ViewType, var orderItemLength: Int = 0) {

    enum class ViewType(val value: Int) {
        ORDER_STATUS(0), ADD_TO_LIST_LAYOUT(1), HEADER(2), COMMERCE_ITEM(3), VIEW_TAX_INVOICE(4), CANCEL_ORDER(5), GIFT(6),ORDER_TOTAL(7)
        ,CHAT_VIEW(8),TRACK_ORDER(9), ENDLESS_AISLE_BARCODE(10)
    }
}
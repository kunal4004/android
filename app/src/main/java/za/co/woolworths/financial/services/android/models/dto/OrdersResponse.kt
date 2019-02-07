package za.co.woolworths.financial.services.android.models.dto

class OrdersResponse {
    var httpCode: Int = 0
    var response: Response? = null
    var totalOrderCount: Int = 0
    var upcomingOrders: ArrayList<UpcomingOrder> = null!!
    var pastOrders: ArrayList<PastOrder> = null!!
}
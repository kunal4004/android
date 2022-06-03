package za.co.woolworths.financial.services.android.models.dto

data class ProductsRequestParams(
    var searchTerm: String,
    var searchType: SearchType,
    var responseType: ResponseType,
    var pageOffset: Int
) {

    @kotlin.jvm.JvmField
    var isUserBrowsing: Boolean = false
    var refinement: String = ""
    var sortOption: String = ""
    var filterContent: Boolean = false
    var deliveryDetails = ""


    enum class ResponseType(val value: String) {
        DETAIL("detail"), SUMMARY("summary")
    }

    enum class SearchType(val value: String) {
        SEARCH("search"), BARCODE("barcode"), NAVIGATE("navigate")
    }
}
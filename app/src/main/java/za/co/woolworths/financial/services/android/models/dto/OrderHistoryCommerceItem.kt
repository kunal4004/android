package za.co.woolworths.financial.services.android.models.dto

class OrderHistoryCommerceItem : CommerceItem() {

    var isSelected = false

    //Grey out the quantity counter so it cannot be clicked until inventory call done
    var inventoryCallCompleted = false

    //select your delivery location address
    var delivery_location: String? = null

    var userQuantity = 0
}
package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

data class Item(
    val PROMOTION: String,
    val id: String,
    val price: Float,
    val defaultPrice:Double,
    val title: String,
    val imageLink: String,
    val ratings: Double,
)
package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components

sealed class BackInStockScreenEvents {

    object RetryClick : BackInStockScreenEvents()
    object ConfirmClick: BackInStockScreenEvents()
    object CancelClick: BackInStockScreenEvents()
    object ColorDropdownClick: BackInStockScreenEvents()
    object SizeDropdownClick: BackInStockScreenEvents()

    data class OnColorSelected(val selectedColor : String): BackInStockScreenEvents()
    data class OnSizeSelected(val selectedSize : String): BackInStockScreenEvents()
}

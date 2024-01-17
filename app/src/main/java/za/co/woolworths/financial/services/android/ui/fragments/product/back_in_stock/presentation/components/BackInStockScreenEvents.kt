package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components

sealed class BackInStockScreenEvents {

    object CreateListClick : BackInStockScreenEvents()
    object RetryClick : BackInStockScreenEvents()
    object ConfirmClick: BackInStockScreenEvents()
    object CancelClick: BackInStockScreenEvents()
    object ColorSpinnerClick: BackInStockScreenEvents()
    object SizeSpinnerClick: BackInStockScreenEvents()
    object EmailEditTextClick: BackInStockScreenEvents()

    data class onColorSelected(val selectedColor : String): BackInStockScreenEvents()
    data class onSizeSelected(val selectedSize : String): BackInStockScreenEvents()
}

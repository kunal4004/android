package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components

import za.co.woolworths.financial.services.android.models.dto.OtherSkus

sealed class BackInStockScreenEvents {

    object ConfirmClick: BackInStockScreenEvents()
    object CancelClick: BackInStockScreenEvents()
    object ColorDropdownClick: BackInStockScreenEvents()
    object SizeDropdownClick: BackInStockScreenEvents()

    data class OnColorSelected(val selectedColor : String): BackInStockScreenEvents()
    data class OnSizeSelected(val selectedSize : String): BackInStockScreenEvents()
    data class OnOtherSKusSelected(val otherSkus: OtherSkus): BackInStockScreenEvents()
}

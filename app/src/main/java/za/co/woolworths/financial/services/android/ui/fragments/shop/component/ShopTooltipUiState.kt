package za.co.woolworths.financial.services.android.ui.fragments.shop.component

sealed class ShopTooltipUiState {

    object Hidden: ShopTooltipUiState()

    object StandardTooltip: ShopTooltipUiState()
    object CNCTooltip: ShopTooltipUiState()
    data class DashTooltip(
        val visibility: Boolean = false,
        val changeButtonVisibility: Boolean = false,
        val timeslotText: String = "",
        val itemLimit: Int = 0,
        val deliveryFee: Int = 0
    ) : ShopTooltipUiState()
}


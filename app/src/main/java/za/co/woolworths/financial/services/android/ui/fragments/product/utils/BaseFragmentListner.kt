package za.co.woolworths.financial.services.android.ui.fragments.product.utils

interface BaseFragmentListner {
    fun onBackPressed()
    fun onSelectionChanged()
    fun onPromotionToggled(count: Int, isEnabled: Boolean)
}
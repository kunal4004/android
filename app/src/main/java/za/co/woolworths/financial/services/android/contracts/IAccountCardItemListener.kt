package za.co.woolworths.financial.services.android.contracts

interface IAccountCardItemListener {
    fun onLinkedAccountItemClicked(productGroup: String)
    fun onApplyNowAccountItemClicked(productGroup: String)
}
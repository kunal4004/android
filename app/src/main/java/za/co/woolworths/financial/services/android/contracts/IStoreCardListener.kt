package za.co.woolworths.financial.services.android.contracts

interface IStoreCardListener {
    fun onBlockPermanentCardPermissionGranted() {}
    fun navigateToPreviousFragment(errorDescription: String?) {}
}
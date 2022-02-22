package za.co.woolworths.financial.services.android.ui.fragments.account.store_card.data.remote

interface StoreCardService {
    suspend fun fetchCLIActiveOffer()
}
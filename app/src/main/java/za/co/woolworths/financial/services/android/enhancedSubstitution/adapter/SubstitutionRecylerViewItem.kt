package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

sealed class SubstitutionRecylerViewItem {

    class SubstitutionOptionHeader(
        val searchHint: String
    ) : SubstitutionRecylerViewItem()

    class SubstitutionProducts(
        val id: Int,
        val productTitle: String,
        val productThumbnail: String,
        val promotionText: String,
        val productPrice: String
    ) : SubstitutionRecylerViewItem()
}
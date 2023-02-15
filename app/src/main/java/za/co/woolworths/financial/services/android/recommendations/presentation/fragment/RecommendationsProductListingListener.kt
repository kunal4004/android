package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product

interface RecommendationsProductListingListener {
    fun openProductDetailView(productList: Product)
}
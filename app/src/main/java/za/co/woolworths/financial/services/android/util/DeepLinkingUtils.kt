package za.co.woolworths.financial.services.android.util

import android.net.Uri
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductSearchTypeAndTerm
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams

class DeepLinkingUtils {

    companion object {

        const val DOMAIN_WOOLWORTHS = "woolworths.co.za"
        const val WHITE_LISTED_DOMAIN = "WHITE_LISTED_DOMAIN"

        fun getProductSearchTypeAndSearchTerm(urlString: String): ProductSearchTypeAndTerm {
            val productSearchTypeAndTerm = ProductSearchTypeAndTerm()
            val uri = Uri.parse(urlString)
            uri?.host?.replace("www.", "")?.replace("www-win-qa.", "")?.let { domain ->
                WoolworthsApplication.getWhitelistedDomainsForQRScanner()?.apply {
                    if (domain in this) {
                        when {
                            domain.contains(DOMAIN_WOOLWORTHS, true) -> {
                                var searchTerm = uri.getQueryParameter("Ntt")
                                if (searchTerm.isNullOrEmpty())
                                    searchTerm = uri.getQueryParameter("searchTerm")

                                if (!searchTerm.isNullOrEmpty()) {
                                    productSearchTypeAndTerm.searchTerm = searchTerm
                                    productSearchTypeAndTerm.searchType = ProductsRequestParams.SearchType.SEARCH
                                } else {
                                    searchTerm = uri.pathSegments?.find { it.startsWith("N-") }
                                    if (!searchTerm.isNullOrEmpty()) {
                                        productSearchTypeAndTerm.searchTerm = searchTerm
                                        productSearchTypeAndTerm.searchType = ProductsRequestParams.SearchType.NAVIGATE
                                    } else {
                                        productSearchTypeAndTerm.searchTerm = WHITE_LISTED_DOMAIN
                                    }
                                }
                            }
                            else -> {
                                productSearchTypeAndTerm.searchTerm = WHITE_LISTED_DOMAIN
                            }
                        }

                    }
                }
            }

            return productSearchTypeAndTerm
        }
    }
}
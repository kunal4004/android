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
                            domain.contains(DOMAIN_WOOLWORTHS, true) -> when {
                                !uri.getQueryParameter("Ntt").isNullOrEmpty() -> {
                                    productSearchTypeAndTerm.searchTerm = uri.getQueryParameter("Ntt")!!
                                    productSearchTypeAndTerm.searchType = ProductsRequestParams.SearchType.SEARCH
                                }
                                !uri.getQueryParameter("searchTerm").isNullOrEmpty() -> {
                                    productSearchTypeAndTerm.searchTerm = uri.getQueryParameter("searchTerm")!!
                                    productSearchTypeAndTerm.searchType = if (productSearchTypeAndTerm.searchTerm.startsWith("cat", true)) ProductsRequestParams.SearchType.NAVIGATE else ProductsRequestParams.SearchType.SEARCH
                                }
                                !uri.pathSegments?.find { it.startsWith("N-") }.isNullOrEmpty() -> {
                                    productSearchTypeAndTerm.searchTerm = uri.pathSegments?.find { it.startsWith("N-") }!!
                                    productSearchTypeAndTerm.searchType = ProductsRequestParams.SearchType.NAVIGATE
                                }
                                !uri.pathSegments?.find { it.startsWith("A-") }.isNullOrEmpty() -> {
                                    productSearchTypeAndTerm.searchTerm = uri.pathSegments?.find { it.startsWith("A-") }!!
                                    productSearchTypeAndTerm.searchType = ProductsRequestParams.SearchType.NAVIGATE
                                }
                                else -> {
                                    productSearchTypeAndTerm.searchTerm = WHITE_LISTED_DOMAIN
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
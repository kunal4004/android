package za.co.woolworths.financial.services.android.viewmodels

import android.net.Uri
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductSearchTypeAndTerm
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeScanFragment

class ProductSearchViewModelImpl: ProductSearchViewModel{

    override fun getTypeAndTerm(urlString: String): ProductSearchTypeAndTerm {
        val productSearchTypeAndTerm = ProductSearchTypeAndTerm()
        val uri = Uri.parse(urlString)

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
            }else{

            }
        }

        return productSearchTypeAndTerm;
    }
}


package za.co.woolworths.financial.services.android.viewmodels

import za.co.woolworths.financial.services.android.models.dto.ProductSearchTypeAndTerm

interface ProductSearchViewModel {

    fun getTypeAndTerm(urlString: String): ProductSearchTypeAndTerm;
}